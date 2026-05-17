package bioCanteenApp.menu.controller;

import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Product;
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

class MenuControllerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IMenuService menuService;
    private DishService dishService;
    private IProvisioningService provisioningService;
    private IMenuMapper menuMapper;

    private MenuController controller;

    @BeforeEach
    void setUp() {
        menuService = mock(IMenuService.class);
        dishService = mock(DishService.class);
        provisioningService = mock(IProvisioningService.class);
        menuMapper = mock(IMenuMapper.class);

        controller = new MenuController(
                menuService,
                dishService,
                provisioningService,
                menuMapper
        );
    }

    @Test
    void shouldGetAllMenus() {
        List<MenuDto> menus = List.of(
                createMenuDto(
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 17)
                ),
                createMenuDto(
                        LocalDate.of(2026, 5, 18),
                        LocalDate.of(2026, 5, 24)
                )
        );

        when(menuService.getAllMenus())
                .thenReturn(menus);

        List<MenuDto> result = controller.getAllMenus();

        assertEquals(menus, result);

        verify(menuService).getAllMenus();
    }

    @Test
    void shouldCreateMenu() {
        MenuDto dto = createMenuDto(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17)
        );

        when(menuService.createMenu(dto))
                .thenReturn(dto);

        MenuDto result = controller.createMenu(dto);

        assertEquals(dto, result);

        verify(menuService).createMenu(dto);
    }

    @Test
    void shouldGenerateMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        MenuDto menuDto = createMenuDto(start, end);
        Menu menu = new Menu(start, end, null);

        when(menuService.generateMenu(start, end))
                .thenReturn(menuDto);

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        MenuDto result = controller.generateMenu(
                "2026-05-11",
                "2026-05-17"
        );

        assertEquals(menuDto, result);

        verify(menuService).generateMenu(start, end);
        verify(menuMapper).toDomain(menuDto);
        verify(provisioningService).getPlannedQuantities(menu);
    }

    @Test
    void shouldGetMenusByWeek() {
        MenuDto menu1 = createMenuDto(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17)
        );

        MenuDto menu2 = createMenuDto(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24)
        );

        when(menuService.getAllMenus())
                .thenReturn(List.of(menu1, menu2));

        ResponseEntity<List<MenuDto>> response =
                controller.getMenusByWeek(
                        LocalDate.of(2026, 5, 12),
                        LocalDate.of(2026, 5, 16)
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(menu1), response.getBody());

        verify(menuService).getAllMenus();
    }

    @Test
    void shouldPublishMenu() {
        ResponseEntity<Void> response =
                controller.publishMenu(
                        "2026-05-11",
                        "2026-05-17",
                        1L
                );

        assertEquals(200, response.getStatusCode().value());

        verify(menuService).publishMenu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                1L
        );
    }

    @Test
    void shouldCloseMenu() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 17);

        MenuDto menuDto = createMenuDto(start, end);
        Menu menu = new Menu(start, end, null);

        when(menuService.getMenusByWeek(start, end))
                .thenReturn(List.of(menuDto));

        when(menuMapper.toDomain(menuDto))
                .thenReturn(menu);

        when(provisioningService.findPlanned(menu))
                .thenReturn(Optional.empty());

        ResponseEntity<Void> response =
                controller.closeMenu(
                        "2026-05-11",
                        "2026-05-17"
                );

        assertEquals(200, response.getStatusCode().value());

        verify(menuService).closeMenu(start, end);
        verify(menuService).getMenusByWeek(start, end);
        verify(menuMapper, times(2)).toDomain(menuDto);
        verify(provisioningService).findPlanned(menu);
        verify(provisioningService).getAdjustedQuantities(menu);
    }

    @Test
    void shouldGetPlanningStats() {
        Map<String, Object> stats = Map.of(
                "totalMenus", 10L,
                "totalDishes", 5L,
                "approvalRate", "50%"
        );

        when(menuService.getPlanningStats())
                .thenReturn(stats);

        ResponseEntity<Map<String, Object>> response =
                controller.getPlanningStats();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(stats, response.getBody());

        verify(menuService).getPlanningStats();
    }

    private MenuDto createMenuDto(LocalDate start, LocalDate end) {
        MenuDto dto = new MenuDto();
        dto.setWeekStartDate(start);
        dto.setWeekEndDate(end);
        return dto;
    }
}