package bioCanteenApp.provisioning.service;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.provisioning.dto.ProductionOrderDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IProvisioningService {
    Map<Product, Double> adjustQuantitiesAndAlert(
            Menu menu,
            Map<Product, Double> planned
    );

    Map<ProductDTO, Double> calculatePlannedQuantities(MenuDto menuDto);

    Map<Product, Double> getPlannedQuantities(Menu menu);

    Map<Product, Double> getAdjustedQuantities(Menu menu);
    Optional<Map<Product, Double>> findPlanned(Menu menu);
    Optional<Map<Product, Double>> findAdjusted(Menu menu);
    List<ProductionOrderDTO> generateProductionPlan(Menu menu, Map<Product, Double> productNeeds);
    Map<Product, Double> recalculatePlanned(Menu menu);
    Map<Product, Double> calculateNextWeekProductNeedsFromCurrentWeekReservations();



}

