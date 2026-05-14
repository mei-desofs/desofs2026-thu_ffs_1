package bioCanteenApp.menu.controller;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.dto.MenuEntryDishDto;
import bioCanteenApp.menu.dto.MenuEntryDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.provisioning.service.IProvisioningService;
import bioCanteenApp.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final IMenuService menuService;
    private final DishService dishService;
    private final IProvisioningService provisioningService;
    private final IMenuMapper menuMapper;

    @GetMapping
    public List<MenuDto> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PostMapping
    public MenuDto createMenu(@RequestBody MenuDto dto) {
        return menuService.createMenu(dto);
    }

    @GetMapping("/generate")
    public MenuDto generateMenu(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        MenuDto menu = menuService.generateMenu(java.time.LocalDate.parse(startDate), java.time.LocalDate.parse(endDate));
        provisioningService.getPlannedQuantities(menuMapper.toDomain(menu));
        return menu;
    }

    @GetMapping("/week")
    public ResponseEntity<List<MenuDto>> getMenusByWeek(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MenuDto> menus = menuService.getAllMenus().stream()
                .filter(m -> !m.getWeekStartDate().isAfter(endDate) && !m.getWeekEndDate().isBefore(startDate))
                .collect(Collectors.toList());

        return ResponseEntity.ok(menus);
    }

    @PostMapping("/publish")
    public ResponseEntity<Void> publishMenu(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("dietitianId") Long dietitianId
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        menuService.publishMenu(start, end, dietitianId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeMenu(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        menuService.closeMenu(start, end);

        MenuDto menu = menuService.getMenusByWeek(start, end).get(0);

        Optional<Map<Product, Double>> planned = provisioningService.findPlanned(menuMapper.toDomain(menu));
        provisioningService.getAdjustedQuantities(
                menuMapper.toDomain(menu)
        );
        return ResponseEntity.ok().build();
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlanningStats() {
        Map<String, Object> stats = menuService.getPlanningStats();
        return ResponseEntity.ok(stats);
    }

}