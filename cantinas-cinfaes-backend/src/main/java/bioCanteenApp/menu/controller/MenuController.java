package bioCanteenApp.menu.controller;
import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.provisioning.service.IProvisioningService;
import bioCanteenApp.utils.exceptions.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Slf4j
public class MenuController {

    private final IMenuService menuService;
    private final DishService dishService;
    private final IProvisioningService provisioningService;
    private final IMenuMapper menuMapper;

    @GetMapping
    public List<MenuDto> getAllMenus() {

        log.info("Fetching all menus");

        List<MenuDto> menus =
                menuService.getAllMenus();

        log.info("Found {} menus", menus.size());

        return menus;
    }

    @PostMapping
    public MenuDto createMenu(@RequestBody MenuDto dto) {

        log.info(
                "Creating menu from {} to {}",
                LogSanitizer.sanitize(dto.getWeekStartDate()),
                LogSanitizer.sanitize(dto.getWeekEndDate())
        );

        MenuDto createdMenu =
                menuService.createMenu(dto);

        log.info(
                "Menu created successfully with id: {}",
                createdMenu.getId()
        );

        return createdMenu;
    }

    @GetMapping("/generate")
    public MenuDto generateMenu(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {

        log.info(
                "Generating menu from {} to {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        MenuDto menu =
                menuService.generateMenu(
                        java.time.LocalDate.parse(startDate),
                        java.time.LocalDate.parse(endDate)
                );

        provisioningService.getPlannedQuantities(
                menuMapper.toDomain(menu)
        );

        log.info(
                "Menu generated successfully with id: {}",
                menu.getId()
        );

        return menu;
    }

    @GetMapping("/week")
    public ResponseEntity<List<MenuDto>> getMenusByWeek(
            @RequestParam(name = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(name = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        log.info(
                "Fetching menus between {} and {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        List<MenuDto> menus = menuService.getAllMenus().stream()
                .filter(m -> !m.getWeekStartDate().isAfter(endDate)
                        && !m.getWeekEndDate().isBefore(startDate))
                .collect(Collectors.toList());

        log.info(
                "Found {} menus between {} and {}",
                menus.size(),
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        return ResponseEntity.ok(menus);
    }

    @PostMapping("/publish")
    public ResponseEntity<Void> publishMenu(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("dietitianId") Long dietitianId
    ) {

        log.warn(
                "Publishing menu from {} to {} by dietitian id: {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate),
                LogSanitizer.sanitize(dietitianId)
        );

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        menuService.publishMenu(start, end, dietitianId);

        log.info(
                "Menu published successfully from {} to {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeMenu(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {

        log.warn(
                "Closing menu from {} to {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        menuService.closeMenu(start, end);

        MenuDto menu =
                menuService.getMenusByWeek(start, end).get(0);

        provisioningService.findPlanned(menuMapper.toDomain(menu));

        provisioningService.getAdjustedQuantities(
                menuMapper.toDomain(menu)
        );

        log.info(
                "Menu closed successfully from {} to {}",
                LogSanitizer.sanitize(startDate),
                LogSanitizer.sanitize(endDate)
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlanningStats() {

        log.info("Fetching menu planning statistics");

        Map<String, Object> stats =
                menuService.getPlanningStats();

        log.info("Menu planning statistics fetched successfully");

        return ResponseEntity.ok(stats);
    }
}