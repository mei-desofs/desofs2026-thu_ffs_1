package bioCanteenApp.provisioning.controller;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.dto.ProductQuantityDTO;
import bioCanteenApp.products.mapper.IProductMapper;
import bioCanteenApp.provisioning.service.IProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProvisioningControllerTest {

    private IProvisioningService provisioningService;
    private IProductMapper productMapper;
    private IMenuService menuService;
    private IMenuMapper menuMapper;
    private IMenuRepo menuRepository;

    private ProvisioningController controller;

    @BeforeEach
    void setUp() {
        provisioningService = mock(IProvisioningService.class);
        productMapper = mock(IProductMapper.class);
        menuService = mock(IMenuService.class);
        menuMapper = mock(IMenuMapper.class);
        menuRepository = mock(IMenuRepo.class);

        controller = new ProvisioningController(
                provisioningService,
                productMapper,
                menuService,
                menuMapper
        );
    }

    @Test
    void shouldGetPlannedQuantities() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.getPlannedQuantities(menu))
                .thenReturn(Map.of(product, 10.0));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.getPlannedQuantities(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).getPlannedQuantities(menu);
    }

    @Test
    void shouldGetAdjustedQuantities() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.getAdjustedQuantities(menu))
                .thenReturn(Map.of(product, 15.0));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.getAdjustedQuantities(
                        1L,
                        List.of()
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).getAdjustedQuantities(menu);
    }

    @Test
    void shouldFindPlanned() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.findPlanned(menu))
                .thenReturn(Optional.of(Map.of(product, 20.0)));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.findPlanned(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).findPlanned(menu);
    }

    @Test
    void shouldReturnNoContentWhenPlannedNotFound() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.findPlanned(menu))
                .thenReturn(Optional.empty());

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.findPlanned(1L);

        assertEquals(204, response.getStatusCode().value());

        verify(provisioningService).findPlanned(menu);
    }

    @Test
    void shouldFindAdjusted() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.findAdjusted(menu))
                .thenReturn(Optional.of(Map.of(product, 25.0)));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.findAdjusted(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).findAdjusted(menu);
    }

//
    @Test
    void shouldGetUpdatedPlannedWhenMenuNotClosed() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.GENERATED);

        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.recalculatePlanned(menu))
                .thenReturn(Map.of(product, 40.0));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.getUpdatedPlanned(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).recalculatePlanned(menu);
    }

    @Test
    void shouldGetUpdatedPlannedWhenMenuClosed() {
        MenuDto menuDto = createMenuDto(1L);
        Menu menu = createMenu(MenuStatus.CLOSED);


        Product product = new Product(
                "Rice",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );        ProductDTO productDTO = new ProductDTO();

        when(menuService.getAllMenus())
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.getPlannedQuantities(menu))
                .thenReturn(Map.of(product, 50.0));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        ResponseEntity<List<ProductQuantityDTO>> response =
                controller.getUpdatedPlanned(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(provisioningService).getPlannedQuantities(menu);
    }

    private MenuDto createMenuDto(Long id) {
        MenuDto dto = new MenuDto();
        dto.setId(id);
        dto.setWeekStartDate(LocalDate.of(2026, 5, 11));
        dto.setWeekEndDate(LocalDate.of(2026, 5, 17));
        return dto;
    }

    private Menu createMenu(MenuStatus status) {
        return new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                status
        );
    }
}