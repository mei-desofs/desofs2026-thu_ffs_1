package bioCanteenApp.menu.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.repository.DishRepo;
import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.MenuEntryMapper;
import bioCanteenApp.menu.mapper.MenuMapper;
import bioCanteenApp.menu.repository.MenuRepo;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.repository.ProductBatchRepo;
import bioCanteenApp.products.service.ProductService;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.users.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private MenuRepo menuRepo;
    private MenuMapper menuMapper;
    private IUserService userService;
    private ProductService productService;
    private DishService dishService;
    private DishRepo dishRepo;
    private MenuEntryMapper menuEntryMapper;
    private UserMapper userMapper;
    private UserRepo userRepository;
    private ProductBatchRepo productBatchRepo;

    private MenuService service;

    @BeforeEach
    void setUp() {
        menuRepo = mock(MenuRepo.class);
        menuMapper = mock(MenuMapper.class);
        userService = mock(IUserService.class);
        productService = mock(ProductService.class);
        dishService = mock(DishService.class);
        dishRepo = mock(DishRepo.class);
        menuEntryMapper = mock(MenuEntryMapper.class);
        userMapper = mock(UserMapper.class);
        userRepository = mock(UserRepo.class);
        productBatchRepo = mock(ProductBatchRepo.class);

        service = new MenuService(
                menuRepo,
                menuMapper,
                userService,
                productService,
                dishService,
                dishRepo,
                menuEntryMapper,
                userMapper,
                userRepository,
                productBatchRepo
        );
    }

    @Test
    void shouldGetAllMenus() {
        Menu menu1 = new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                MenuStatus.GENERATED
        );

        Menu menu2 = new Menu(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24),
                MenuStatus.GENERATED
        );

        MenuDto dto1 = mock(MenuDto.class);
        MenuDto dto2 = mock(MenuDto.class);

        when(menuRepo.findAll()).thenReturn(List.of(menu1, menu2));
        when(menuMapper.toDTO(menu1)).thenReturn(dto1);
        when(menuMapper.toDTO(menu2)).thenReturn(dto2);

        List<MenuDto> result = service.getAllMenus();

        assertEquals(List.of(dto1, dto2), result);
        verify(menuMapper).toDTO(menu1);
        verify(menuMapper).toDTO(menu2);
    }

    @Test
    void shouldGetMenusByWeek() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        Menu menu = new Menu(start, end, MenuStatus.GENERATED);
        MenuDto dto = mock(MenuDto.class);

        when(menuRepo.findByMenuDates(start, end, start, end))
                .thenReturn(List.of(menu));
        when(menuMapper.toDTO(menu)).thenReturn(dto);

        List<MenuDto> result = service.getMenusByWeek(start, end);

        assertEquals(List.of(dto), result);
        verify(menuRepo).findByMenuDates(start, end, start, end);
        verify(menuMapper).toDTO(menu);
    }

    @Test
    void shouldThrowWhenDieticianDoesNotExistOnCreateMenu() {
        MenuDto dto = mock(MenuDto.class);
        User mockDietician = mock(User.class);

        when(dto.getDieticianId()).thenReturn(mockDietician);
        when(mockDietician.getId()).thenReturn(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createMenu(dto)
        );

        verify(menuRepo, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserIsNotDieticianOnCreateMenu() {
        MenuDto dto = mock(MenuDto.class);
        GetUserDTO userDto = mock(GetUserDTO.class);
        User mockDietician = mock(User.class);

        when(dto.getDieticianId()).thenReturn(mockDietician);
        when(userDto.getRole()).thenReturn(Role.USER.name());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createMenu(dto)
        );

        verify(menuRepo, never()).save(any());
    }

    @Test
    void shouldGenerateMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        ProductDTO productDTO = mock(ProductDTO.class);
        when(productDTO.getName()).thenReturn("Rice");

        DishIngredientDto ingredientDto = new DishIngredientDto(
                1L,
                1L,
                "Rice",
                1.0,
                "kg"
        );

        DishDto dishDto = new DishDto(
                1L,
                "Rice Dish",
                "Info",
                DishType.VEGETARIAN.name(),
                null,
                List.of(ingredientDto)
        );

        Dish dish = new Dish("Rice Dish", DishType.VEGETARIAN);

        MenuDto menuDto = mock(MenuDto.class);

        when(productService.getAvailableSeasonalProducts())
                .thenReturn(List.of(productDTO));

        when(dishService.getDishesWithSeasonalIngredients(List.of("Rice")))
                .thenReturn(List.of(dishDto));

        when(productBatchRepo.sumValidStockByProduct(1L))
                .thenReturn(10.0);

        when(dishRepo.findById(1L))
                .thenReturn(Optional.of(dish));

        when(menuRepo.save(any(Menu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(menuMapper.toDTO(any(Menu.class)))
                .thenReturn(menuDto);

        MenuDto result = service.generateMenu(start, end);

        assertEquals(menuDto, result);
        verify(menuRepo).save(any(Menu.class));
        verify(menuMapper).toDTO(any(Menu.class));
    }

    @Test
    void shouldThrowWhenGenerateMenuHasNoDishesWithStock() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        ProductDTO productDTO = mock(ProductDTO.class);
        when(productDTO.getName()).thenReturn("Rice");

        DishIngredientDto ingredientDto = new DishIngredientDto(
                1L,
                1L,
                "Rice",
                100.0,
                "kg"
        );

        DishDto dishDto = new DishDto(
                1L,
                "Rice Dish",
                "Info",
                DishType.VEGETARIAN.name(),
                null,
                List.of(ingredientDto)
        );

        when(productService.getAvailableSeasonalProducts())
                .thenReturn(List.of(productDTO));

        when(dishService.getDishesWithSeasonalIngredients(List.of("Rice")))
                .thenReturn(List.of(dishDto));

        when(productBatchRepo.sumValidStockByProduct(1L))
                .thenReturn(10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateMenu(start, end)
        );

        verify(menuRepo, never()).save(any());
    }

    @Test
    void shouldPublishMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        User dietician = new User(
                "dietitian@email.com",
                "Dietitian",
                "password",
                Role.DIETITIAN
        );

        Menu menu = new Menu(start, end, MenuStatus.GENERATED);

        when(userRepository.findById(1L)).thenReturn(dietician);
        when(menuRepo.findByMenuDates(start, end, start, end))
                .thenReturn(List.of(menu));

        service.publishMenu(start, end, 1L);

        assertEquals(MenuStatus.PUBLISHED, menu.getStatus());
        assertEquals(dietician, menu.getDietician());

        verify(menuRepo).save(menu);
    }

    @Test
    void shouldThrowWhenDieticianNotFoundOnPublishMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        when(userRepository.findById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.publishMenu(start, end, 1L)
        );

        verify(menuRepo, never()).save(any());
    }

    @Test
    void shouldCloseMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        Menu menu = new Menu(start, end, MenuStatus.PUBLISHED);

        when(menuRepo.findByMenuDates(start, end, start, end))
                .thenReturn(List.of(menu));

        service.closeMenu(start, end);

        assertEquals(MenuStatus.CLOSED, menu.getStatus());
        verify(menuRepo).save(menu);
    }

    @Test
    void shouldGetPlanningStats() {
        when(menuRepo.count()).thenReturn(10L);
        when(dishRepo.count()).thenReturn(5L);
        when(menuRepo.countByStatus(MenuStatus.PUBLISHED)).thenReturn(4L);

        Map<String, Object> result = service.getPlanningStats();

        assertEquals(10L, result.get("totalMenus"));
        assertEquals(5L, result.get("totalDishes"));
        assertEquals("40%", result.get("approvalRate"));
    }

    @Test
    void shouldReturnZeroApprovalRateWhenThereAreNoMenus() {
        when(menuRepo.count()).thenReturn(0L);
        when(dishRepo.count()).thenReturn(5L);
        when(menuRepo.countByStatus(MenuStatus.PUBLISHED)).thenReturn(0L);

        Map<String, Object> result = service.getPlanningStats();

        assertEquals(0L, result.get("totalMenus"));
        assertEquals(5L, result.get("totalDishes"));
        assertEquals("0%", result.get("approvalRate"));
    }
}