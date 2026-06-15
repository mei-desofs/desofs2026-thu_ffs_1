package bioCanteenApp.provisioning.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.menu.repository.MenuRepo;
import bioCanteenApp.notifications.repository.INotificationRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.mapper.IProductMapper;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.provisioning.domain.ProvisioningItem;
import bioCanteenApp.provisioning.domain.ProvisioningType;
import bioCanteenApp.provisioning.dto.ProductionOrderDTO;
import bioCanteenApp.provisioning.repository.IProvisioningItemRepo;
import bioCanteenApp.reservation.repository.IReservationRepo;
import bioCanteenApp.suppliers.domain.SupplierApplication;
import bioCanteenApp.suppliers.domain.SupplierCapacity;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProvisioningServiceTest {

    private IReservationRepo reservationRepo;
    private IProductRepo productRepo;
    private IMenuMapper menuMapper;
    private IProductMapper productMapper;
    private INotificationRepo notificationRepo;
    private IUserRepo userRepo;
    private IProvisioningItemRepo provisioningItemRepo;
    private ISupplierRepo supplierRepo;
    private IMenuRepo menuRepo;

    private ProvisioningService service;

    @BeforeEach
    void setUp() {
        reservationRepo = mock(IReservationRepo.class);
        productRepo = mock(IProductRepo.class);
        menuMapper = mock(IMenuMapper.class);
        productMapper = mock(IProductMapper.class);
        notificationRepo = mock(INotificationRepo.class);
        userRepo = mock(IUserRepo.class);
        provisioningItemRepo = mock(IProvisioningItemRepo.class);
        supplierRepo = mock(ISupplierRepo.class);
        menuRepo = mock(IMenuRepo.class);

        service = new ProvisioningService(
                reservationRepo,
                productRepo,
                menuMapper,
                productMapper,
                notificationRepo,
                userRepo,
                provisioningItemRepo,
                supplierRepo,
                menuRepo
        );
    }

    @Test
    void shouldCalculatePlannedQuantities() {
        Product product = createProduct("Rice");
        ProductDTO productDTO = createProductDTO("Rice");

        Ingredient ingredient = new Ingredient("Rice", 2.0, product);

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);
        dish.setId(1L);
        dish.addIngredient(ingredient, 3.0);

        Menu menu = createMenu();
        menu.setId(10L);

        MenuEntry entry = createMenuEntry(menu);
        MenuEntryDish menuEntryDish = new MenuEntryDish(entry, dish);
        entry.setMenuEntryDishes(List.of(menuEntryDish));
        menu.setEntries(List.of(entry));

        MenuDto menuDto = new MenuDto();

        when(menuMapper.toDomain(menuDto)).thenReturn(menu);
        when(reservationRepo.averageReservationsForDish(1L, 10L)).thenReturn(5L);
        when(reservationRepo.countBackMenusReservations(1L, 10L)).thenReturn(2L);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        Map<ProductDTO, Double> result = service.calculatePlannedQuantities(menuDto);

        assertEquals(1, result.size());
        assertEquals(30.0, result.get(productDTO));

        verify(productMapper).toDTO(product);
    }

    @Test
    void shouldIgnoreDishWhenNoMenusWereAnalyzed() {
        Product product = createProduct("Rice");
        Ingredient ingredient = new Ingredient("Rice", 2.0, product);

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);
        dish.setId(1L);
        dish.addIngredient(ingredient, 3.0);

        Menu menu = createMenu();
        menu.setId(10L);

        MenuEntry entry = createMenuEntry(menu);
        MenuEntryDish menuEntryDish = new MenuEntryDish(entry, dish);
        entry.setMenuEntryDishes(List.of(menuEntryDish));
        menu.setEntries(List.of(entry));

        MenuDto menuDto = new MenuDto();

        when(menuMapper.toDomain(menuDto)).thenReturn(menu);
        when(reservationRepo.averageReservationsForDish(1L, 10L)).thenReturn(5L);
        when(reservationRepo.countBackMenusReservations(1L, 10L)).thenReturn(0L);

        Map<ProductDTO, Double> result = service.calculatePlannedQuantities(menuDto);

        assertTrue(result.isEmpty());
        verify(productMapper, never()).toDTO(any());
    }

    @Test
    void shouldAdjustQuantitiesAndSendDeviationNotification() {
        Product product = createProduct("Rice");

        Ingredient ingredient = new Ingredient("Rice", 2.0, product);

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);
        dish.setId(1L);
        dish.addIngredient(ingredient, 3.0);

        Menu menu = createMenu();

        MenuEntry entry = createMenuEntry(menu);
        MenuEntryDish menuEntryDish = new MenuEntryDish(entry, dish);
        menuEntryDish.setId(5L);

        entry.setMenuEntryDishes(List.of(menuEntryDish));
        menu.setEntries(List.of(entry));

        User manager = new User(
                "manager@email.com",
                "Manager",
                "password",
                Role.CANTEEN_MANAGER
        );

        when(userRepo.findCentralCanteenManager()).thenReturn(manager);
        when(reservationRepo.countConfirmedByMenuEntryDish(5L)).thenReturn(10L);

        Map<Product, Double> planned = Map.of(product, 10.0);

        Map<Product, Double> result = service.adjustQuantitiesAndAlert(menu, planned);

        assertEquals(1, result.size());
        assertEquals(60.0, result.get(product));

        verify(notificationRepo).save(any());
    }

    @Test
    void shouldAdjustQuantitiesWithoutNotificationWhenDeviationIsLow() {
        Product product = createProduct("Rice");

        Ingredient ingredient = new Ingredient("Rice", 2.0, product);

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);
        dish.setId(1L);
        dish.addIngredient(ingredient, 3.0);

        Menu menu = createMenu();

        MenuEntry entry = createMenuEntry(menu);
        MenuEntryDish menuEntryDish = new MenuEntryDish(entry, dish);
        menuEntryDish.setId(5L);

        entry.setMenuEntryDishes(List.of(menuEntryDish));
        menu.setEntries(List.of(entry));

        User manager = new User(
                "manager@email.com",
                "Manager",
                "password",
                Role.CANTEEN_MANAGER
        );

        when(userRepo.findCentralCanteenManager()).thenReturn(manager);
        when(reservationRepo.countConfirmedByMenuEntryDish(5L)).thenReturn(10L);

        Map<Product, Double> planned = Map.of(product, 60.0);

        Map<Product, Double> result = service.adjustQuantitiesAndAlert(menu, planned);

        assertEquals(60.0, result.get(product));
        verify(notificationRepo, never()).save(any());
    }

    @Test
    void shouldReturnStoredPlannedQuantities() {
        Product product = createProduct("Rice");

        Menu menu = createMenu();
        menu.setId(1L);

        ProvisioningItem item = new ProvisioningItem(
                menu,
                product,
                20.0,
                ProvisioningType.PLANNED,
                java.time.LocalDateTime.now()
        );

        when(provisioningItemRepo.findByMenuAndType(1L, ProvisioningType.PLANNED))
                .thenReturn(List.of(item));

        Map<Product, Double> result = service.getPlannedQuantities(menu);

        assertEquals(1, result.size());
        assertEquals(20.0, result.get(product));

        verify(provisioningItemRepo, never()).save(any());
    }

    @Test
    void shouldFindPlannedWhenStoredExists() {
        Product product = createProduct("Rice");

        Menu menu = createMenu();
        menu.setId(1L);

        ProvisioningItem item = new ProvisioningItem(
                menu,
                product,
                20.0,
                ProvisioningType.PLANNED,
                java.time.LocalDateTime.now()
        );

        when(provisioningItemRepo.findByMenuAndType(1L, ProvisioningType.PLANNED))
                .thenReturn(List.of(item));

        Optional<Map<Product, Double>> result = service.findPlanned(menu);

        assertTrue(result.isPresent());
        assertEquals(20.0, result.get().get(product));
    }

    @Test
    void shouldReturnEmptyWhenNoPlannedStoredExists() {
        Menu menu = createMenu();
        menu.setId(1L);

        when(provisioningItemRepo.findByMenuAndType(1L, ProvisioningType.PLANNED))
                .thenReturn(List.of());

        Optional<Map<Product, Double>> result = service.findPlanned(menu);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAdjustedWhenStoredExists() {
        Product product = createProduct("Rice");

        Menu menu = createMenu();
        menu.setId(1L);

        ProvisioningItem item = new ProvisioningItem(
                menu,
                product,
                30.0,
                ProvisioningType.ADJUSTED,
                java.time.LocalDateTime.now()
        );

        when(provisioningItemRepo.findByMenuAndType(1L, ProvisioningType.ADJUSTED))
                .thenReturn(List.of(item));

        Optional<Map<Product, Double>> result = service.findAdjusted(menu);

        assertTrue(result.isPresent());
        assertEquals(30.0, result.get().get(product));
    }

    @Test
    void shouldGenerateProductionPlanWithAvailableSupplier() {
        Product product = createProduct("Rice");

        Menu menu = createMenu();

        SupplierCapacity capacity = new SupplierCapacity(
                "Rice",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                100.0
        );

        SupplierApplication application = new SupplierApplication(
                "Supplier 1",
                "supplier@email.com",
                "912345678",
                null,
                new byte[]{1, 2, 3},
                123456789L,
                List.of(capacity),
                LocalDate.of(2026, 5, 1)
        );

        when(supplierRepo.findApprovedApplicationsFromNonQuarantinedSuppliers())
                .thenReturn(List.of(application));

        List<ProductionOrderDTO> result =
                service.generateProductionPlan(menu, Map.of(product, 50.0));

        assertEquals(1, result.size());
    }

    @Test
    void shouldGenerateProductionPlanWithShortageWhenNoSupplierAvailable() {
        Product product = createProduct("Rice");

        Menu menu = createMenu();

        when(supplierRepo.findApprovedApplicationsFromNonQuarantinedSuppliers())
                .thenReturn(List.of());

        List<ProductionOrderDTO> result =
                service.generateProductionPlan(menu, Map.of(product, 50.0));

        assertEquals(1, result.size());
    }

    @Test
    void shouldRecalculatePlannedQuantities() {
        Product product = createProduct("Rice");
        ProductDTO productDTO = createProductDTO("Rice");

        Ingredient ingredient = new Ingredient("Rice", 2.0, product);

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);
        dish.setId(1L);
        dish.addIngredient(ingredient, 3.0);

        Menu menu = createMenu();
        menu.setId(10L);

        MenuEntry entry = createMenuEntry(menu);
        MenuEntryDish menuEntryDish = new MenuEntryDish(entry, dish);
        entry.setMenuEntryDishes(List.of(menuEntryDish));
        menu.setEntries(List.of(entry));

        MenuDto menuDto = new MenuDto();

        when(menuMapper.toDTO(menu)).thenReturn(menuDto);
        when(menuMapper.toDomain(menuDto)).thenReturn(menu);
        when(reservationRepo.averageReservationsForDish(1L, 10L)).thenReturn(5L);
        when(reservationRepo.countBackMenusReservations(1L, 10L)).thenReturn(2L);
        when(productMapper.toDTO(product)).thenReturn(productDTO);
        when(productMapper.toDomain(productDTO)).thenReturn(product);

        Map<Product, Double> result = service.recalculatePlanned(menu);

        assertEquals(1, result.size());
        assertEquals(30.0, result.get(product));

        verify(provisioningItemRepo).deleteByMenuAndType(10L, ProvisioningType.PLANNED);
        verify(provisioningItemRepo).save(any());
    }

    private Product createProduct(String name) {
        Product product = new Product(
                name,
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );
        product.setId(1L);
        return product;
    }

    private ProductDTO createProductDTO(String name) {
        return ProductDTO.builder()
                .id(1L)
                .name(name)
                .unit("kg")
                .expirationDays(365)
                .seasonalSeasons(List.of(Season.SPRING.name()))
                .allergens(List.of(Allergen.GLUTEN.name()))
                .build();
    }

    private Menu createMenu() {
        return new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                MenuStatus.GENERATED
        );
    }

    private MenuEntry createMenuEntry(Menu menu) {
        MenuEntry entry = new MenuEntry();
        entry.setMenu(menu);
        entry.setDate(LocalDate.of(2026, 5, 11));
        entry.setWeekDay("MONDAY");
        return entry;
    }
}