package bioCanteenApp.provisioning.controller;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.dto.ProductQuantityDTO;
import bioCanteenApp.products.mapper.IProductMapper;
import bioCanteenApp.provisioning.dto.ProductionOrderDTO;
import bioCanteenApp.provisioning.dto.ProvisioningItemDTO;
import bioCanteenApp.provisioning.service.IProvisioningService;
import bioCanteenApp.provisioning.service.ProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provisioning")
@RequiredArgsConstructor
public class ProvisioningController {

    private final IProvisioningService provisioningService;
    private final IProductMapper productMapper;
    private final IMenuService menuService;
    private final IMenuMapper menuMapper;
    private final IMenuRepo menuRepository;

    /**
     * Calcula as quantidades planejadas para um menu
     */
    @GetMapping("/planned/{id}")
    public ResponseEntity<List<ProductQuantityDTO>> getPlannedQuantities(@PathVariable("id") Long id) {
        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + id));

        Map<Product, Double> planned =
                provisioningService.getPlannedQuantities(menuMapper.toDomain(menu));

        List<ProductQuantityDTO> dtoList = planned.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    /**
     * Ajusta quantidades com base em reservas reais e gera alertas se houver desvio > 10%
     */
    @PostMapping("/adjusted/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> getAdjustedQuantities(
            @PathVariable("menuId") Long menuId,
            @RequestBody List<ProductQuantityDTO> plannedDtos
    ) {

        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + menuId));

        Map<Product, Double> adjusted =
                provisioningService.getAdjustedQuantities(menuMapper.toDomain(menu));

        List<ProductQuantityDTO> dtoList = adjusted.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/planned/find/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> findPlanned(
            @PathVariable("menuId") Long menuId
    ) {
        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + menuId));

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
        MenuDto menu = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + menuId));


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

    /**
     * Gera o plano de produção PREVISTO (distribuído por fornecedores)
     */
    @GetMapping("/production-plan/planned/{menuId}")
    public ResponseEntity<List<ProductionOrderDTO>> getPlannedProductionPlan(@PathVariable("menuId") Long menuId) {
        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found"));

        Menu menu = menuMapper.toDomain(menuDto);
        Map<Product, Double> plannedNeeds = provisioningService.getPlannedQuantities(menu);

        List<ProductionOrderDTO> plan = provisioningService.generateProductionPlan(menu, plannedNeeds);

        return ResponseEntity.ok(plan);
    }

    /**
     * Gera o plano de produção AJUSTADO (após reservas, distribuído por fornecedores)
     */
    @GetMapping("/production-plan/adjusted/{menuId}")
    public ResponseEntity<List<ProductionOrderDTO>> getAdjustedProductionPlan(@PathVariable("menuId") Long menuId) {
        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found"));

        Menu menu = menuMapper.toDomain(menuDto);
        Map<Product, Double> adjustedNeeds = provisioningService.getAdjustedQuantities(menu);

        List<ProductionOrderDTO> plan = provisioningService.generateProductionPlan(menu, adjustedNeeds);

        return ResponseEntity.ok(plan);
    }

    @GetMapping("/planned/update/{menuId}")
    public ResponseEntity<List<ProductQuantityDTO>> getUpdatedPlanned(@PathVariable("menuId") Long menuId) {

        MenuDto menuDto = menuService.getAllMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + menuId));

        Menu menu = menuMapper.toDomain(menuDto);
        Map<Product, Double> result;

        if (menu.getStatus() != MenuStatus.CLOSED) {
            result = provisioningService.recalculatePlanned(menu);
        } else {
            result = provisioningService.getPlannedQuantities(menu);
        }

        List<ProductQuantityDTO> dtoList = result.entrySet().stream()
                .map(e -> ProductQuantityDTO.builder()
                        .product(productMapper.toDTO(e.getKey()))
                        .quantity(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

}
