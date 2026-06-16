package bioCanteenApp.provisioning.controller;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductQuantityDTO;
import bioCanteenApp.products.mapper.IProductMapper;
import bioCanteenApp.provisioning.dto.ProductionOrderDTO;
import bioCanteenApp.provisioning.service.IProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provisioning")
@RequiredArgsConstructor
@Slf4j
public class ProvisioningController {

    private final IProvisioningService provisioningService;
    private final IProductMapper productMapper;
    private final IMenuService menuService;
    private final IMenuMapper menuMapper;

    @GetMapping("/planned/{id}")
    public ResponseEntity<List<ProductQuantityDTO>> getPlannedQuantities(
            @PathVariable("id") Long id
    ) {

        log.info("Fetching planned quantities for menu id: {}", id);

        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found with id: " + id));

        Map<Product, Double> planned =
                provisioningService.getPlannedQuantities(
                        menuMapper.toDomain(menu)
                );

        List<ProductQuantityDTO> dtoList = planned.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info(
                "Calculated planned quantities for {} products in menu id: {}",
                dtoList.size(),
                id
        );

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/adjusted/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> getAdjustedQuantities(
            @PathVariable("menuId") Long menuId
    ) {

        log.info("Fetching adjusted quantities for menu id: {}", menuId);

        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found with id: " + menuId));

        Map<Product, Double> adjusted =
                provisioningService.getAdjustedQuantities(
                        menuMapper.toDomain(menu)
                );

        List<ProductQuantityDTO> dtoList = adjusted.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info(
                "Calculated adjusted quantities for {} products in menu id: {}",
                dtoList.size(),
                menuId
        );

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/planned/find/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> findPlanned(
            @PathVariable("menuId") Long menuId
    ) {

        log.info("Finding stored planned quantities for menu id: {}", menuId);

        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found with id: " + menuId));

        return provisioningService.findPlanned(menuMapper.toDomain(menu))
                .map(map ->
                        map.entrySet().stream()
                                .map(e -> ProductQuantityDTO.builder()
                                        .product(productMapper.toDTO(e.getKey()))
                                        .quantity(e.getValue())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/adjusted/find/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> findAdjusted(
            @PathVariable("menuId") Long menuId
    ) {

        log.info("Finding stored adjusted quantities for menu id: {}", menuId);

        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found with id: " + menuId));

        return provisioningService.findAdjusted(menuMapper.toDomain(menu))
                .map(map ->
                        map.entrySet().stream()
                                .map(e -> ProductQuantityDTO.builder()
                                        .product(productMapper.toDTO(e.getKey()))
                                        .quantity(e.getValue())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/production-plan/planned/{menuId}")
    public ResponseEntity<List<ProductionOrderDTO>> getPlannedProductionPlan(
            @PathVariable("menuId") Long menuId
    ) {

        log.info(
                "Generating planned production plan for menu id: {}",
                menuId
        );

        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found"));

        Menu menu = menuMapper.toDomain(menuDto);

        Map<Product, Double> plannedNeeds =
                provisioningService.getPlannedQuantities(menu);

        List<ProductionOrderDTO> plan =
                provisioningService.generateProductionPlan(
                        menu,
                        plannedNeeds
                );

        log.info(
                "Generated planned production plan with {} orders for menu id: {}",
                plan.size(),
                menuId
        );

        return ResponseEntity.ok(plan);
    }

    @GetMapping("/production-plan/adjusted/{menuId}")
    public ResponseEntity<List<ProductionOrderDTO>> getAdjustedProductionPlan(
            @PathVariable("menuId") Long menuId
    ) {

        log.info(
                "Generating adjusted production plan for menu id: {}",
                menuId
        );

        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found"));

        Menu menu = menuMapper.toDomain(menuDto);

        Map<Product, Double> adjustedNeeds =
                provisioningService.getAdjustedQuantities(menu);

        List<ProductionOrderDTO> plan =
                provisioningService.generateProductionPlan(
                        menu,
                        adjustedNeeds
                );

        log.info(
                "Generated adjusted production plan with {} orders for menu id: {}",
                plan.size(),
                menuId
        );

        return ResponseEntity.ok(plan);
    }

    @GetMapping("/planned/update/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> getUpdatedPlanned(
            @PathVariable("menuId") Long menuId
    ) {

        log.info(
                "Updating planned quantities for menu id: {}",
                menuId
        );

        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu not found with id: " + menuId));

        Menu menu = menuMapper.toDomain(menuDto);

        Map<Product, Double> result;

        if (menu.getStatus() != MenuStatus.CLOSED) {

            log.info(
                    "Recalculating planned quantities for open menu id: {}",
                    menuId
            );

            result = provisioningService.recalculatePlanned(menu);

        } else {

            log.info(
                    "Fetching stored planned quantities for closed menu id: {}",
                    menuId
            );

            result = provisioningService.getPlannedQuantities(menu);
        }

        List<ProductQuantityDTO> dtoList = result.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info(
                "Updated planned quantities generated for {} products in menu id: {}",
                dtoList.size(),
                menuId
        );

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/next-week/needs")
    public ResponseEntity<List<ProductQuantityDTO>> calculateNextWeekNeeds() {
        Map<Product, Double> needs =
                provisioningService.calculateNextWeekProductNeedsFromCurrentWeekReservations();

        List<ProductQuantityDTO> dtoList = needs.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}