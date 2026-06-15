package bioCanteenApp.provisioning.service;

import bioCanteenApp.dish.domain.DishIngredient; // Importante adicionar este import
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationType;
import bioCanteenApp.notifications.repository.INotificationRepo;
import bioCanteenApp.products.domain.Product;
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
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import bioCanteenApp.menu.repository.IMenuRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvisioningService implements IProvisioningService  {

    private final IReservationRepo reservationRepo;
    private final IProductRepo productRepo;
    private final IMenuMapper menuMapper;
    private final IProductMapper productMapper;
    private final INotificationRepo notificationRepo;
    private final IUserRepo userRepo;
    private final IProvisioningItemRepo provisioningItemRepo;
    private final ISupplierRepo supplierRepo;
    private final IMenuRepo menuRepo;

    /**
     * PREVISÃO: histórico + ementa - stock existente
     * Implementa: Reservas * Qtd Prato (Y) * Fator Conversão (Z)
     */
    public Map<ProductDTO, Double> calculatePlannedQuantities(MenuDto menuDto) {
        Menu menu = menuMapper.toDomain(menuDto);
        Map<ProductDTO, Double> planned = new HashMap<>();

        for (MenuEntry entry : menu.getEntries()) {
            for (MenuEntryDish med : entry.getMenuEntryDishes()) {

                long avgReservations = reservationRepo.averageReservationsForDish(
                        med.getDish().getId(), menu.getId());

                long analyzedMenus = reservationRepo.countBackMenusReservations(
                        med.getDish().getId(), menu.getId());

                if (analyzedMenus == 0) continue;

                // AJUSTE: Usar DishIngredient para aceder à tabela intermédia
                for (DishIngredient di : med.getDish().getDishIngredients()) {
                    Ingredient ingredient = di.getIngredient();
                    Product product = ingredient.getProduct();

                    // X: avgReservations
                    // Y: di.getQuantity() (quantidade no prato)
                    // Z: ingredient.getQuantity() (fator de conversão produto/ingrediente)
                    double totalNeeded = avgReservations * di.getQuantity() * ingredient.getQuantity();

                    planned.merge(
                            productMapper.toDTO(product),
                            totalNeeded,
                            Double::sum
                    );
                }
            }
        }
        return planned;
    }

    /**
     * AJUSTE FINAL: reservas reais * Y * Z
     */
    public Map<Product, Double> adjustQuantitiesAndAlert(Menu menu, Map<Product, Double> planned) {
        Map<Product, Double> toOrder = new HashMap<>();
        User manager = userRepo.findCentralCanteenManager();
        Set<Product> notifiedProducts = new HashSet<>();

        for (MenuEntry entry : menu.getEntries()) {
            for (MenuEntryDish med : entry.getMenuEntryDishes()) {

                long realReservations = reservationRepo.countConfirmedByMenuEntryDish(med.getId());

                for (DishIngredient di : med.getDish().getDishIngredients()) {
                    Ingredient ingredient = di.getIngredient();
                    Product product = ingredient.getProduct();

                    // Cálculo com as reservas reais e os dois fatores de quantidade
                    double realNet = realReservations * di.getQuantity() * ingredient.getQuantity();

                    toOrder.merge(product, realNet, Double::sum);
                }
            }
        }

        // Bloco de Alertas de Desvio (comparação por produto total)
        toOrder.forEach((product, realTotal) -> {
            double plannedQty = planned.entrySet().stream()
                    .filter(e -> e.getKey().getName().equals(product.getName()))
                    .mapToDouble(Map.Entry::getValue)
                    .findFirst()
                    .orElse(0.0);

            double deviation = plannedQty == 0 ? 0 : Math.abs(realTotal - plannedQty) / plannedQty;

            if (deviation > 0.10 && !notifiedProducts.contains(product)) {
                sendDeviationNotification(manager, product, plannedQty, realTotal);
                notifiedProducts.add(product);
            }
        });

        return toOrder;
    }

    private void sendDeviationNotification(User manager, Product product, double planned, double real) {
        String title = "Desvio significativo no planeamento de stock";
        String message = String.format(
                "O produto '%s' apresenta um desvio superior a 10%%.\n" +
                        "Planeado: %.2f %s | Real: %.2f %s",
                product.getName(), planned, product.getUnit(), real, product.getUnit()
        );

        Notification notification = new Notification(
                manager, title, message, NotificationType.DEVIATION_ALERT,
                LocalDateTime.now(), false, "/product", 1
        );
        notificationRepo.save(notification);
    }

    // --- OS RESTANTES MÉTODOS (getPlannedQuantities, generateProductionPlan, etc.)
    // MANTÊM-SE IGUAIS AO TEU ORIGINAL, POIS JÁ CHAMAM OS MÉTODOS ACIMA ---

    @Override
    public Map<Product, Double> getPlannedQuantities(Menu menu) {
        List<ProvisioningItem> stored = provisioningItemRepo.findByMenuAndType(menu.getId(), ProvisioningType.PLANNED);
        if (!stored.isEmpty()) {
            return stored.stream().collect(Collectors.toMap(ProvisioningItem::getProduct, ProvisioningItem::getQuantity));
        }
        Map<ProductDTO, Double> calculated = calculatePlannedQuantities(menuMapper.toDTO(menu));
        Map<Product, Double> result = calculated.entrySet().stream()
                .collect(Collectors.toMap(e -> productMapper.toDomain(e.getKey()), Map.Entry::getValue));

        result.forEach((product, qty) -> provisioningItemRepo.save(new ProvisioningItem(menu, product, qty, ProvisioningType.PLANNED, LocalDateTime.now())));
        return result;
    }

    @Override
    public Map<Product, Double> getAdjustedQuantities(Menu menu) {
        List<ProvisioningItem> stored = provisioningItemRepo.findByMenuAndType(menu.getId(), ProvisioningType.ADJUSTED);
        if (!stored.isEmpty()) {
            return stored.stream().collect(Collectors.toMap(ProvisioningItem::getProduct, ProvisioningItem::getQuantity));
        }
        Map<Product, Double> planned = getPlannedQuantities(menu);
        Map<Product, Double> adjusted = adjustQuantitiesAndAlert(menu, planned);

        adjusted.forEach((product, qty) -> provisioningItemRepo.save(new ProvisioningItem(menu, product, qty, ProvisioningType.ADJUSTED, LocalDateTime.now())));
        return adjusted;
    }

    @Override
    public Optional<Map<Product, Double>> findAdjusted(Menu menu) {
        List<ProvisioningItem> stored = provisioningItemRepo.findByMenuAndType(menu.getId(), ProvisioningType.ADJUSTED);
        return stored.isEmpty() ? Optional.empty() : Optional.of(stored.stream().collect(Collectors.toMap(ProvisioningItem::getProduct, ProvisioningItem::getQuantity)));
    }

    @Override
    public Optional<Map<Product, Double>> findPlanned(Menu menu) {
        List<ProvisioningItem> stored = provisioningItemRepo.findByMenuAndType(menu.getId(), ProvisioningType.PLANNED);
        return stored.isEmpty() ? Optional.empty() : Optional.of(stored.stream().collect(Collectors.toMap(ProvisioningItem::getProduct, ProvisioningItem::getQuantity)));
    }

    @Override
    public List<ProductionOrderDTO> generateProductionPlan(Menu menu, Map<Product, Double> productNeeds) {
        List<ProductionOrderDTO> orders = new ArrayList<>();
        for (Map.Entry<Product, Double> entry : productNeeds.entrySet()) {
            Product product = entry.getKey();
            Double quantityNeeded = entry.getValue();

            List<SupplierApplication> eligibleSuppliers =
                    supplierRepo.findApprovedApplicationsFromNonQuarantinedSuppliers()
                            .stream()
                            .filter(app -> hasCapacityForProduct(app, product.getName(), menu.getWeekStartDate()))
                            .toList();

            for (SupplierApplication supplier : eligibleSuppliers) {
                if (quantityNeeded <= 0) break;
                double capacity = getSupplierAvailableCapacity(supplier, product.getName());
                double amountFromThisSupplier = Math.min(quantityNeeded, capacity);
                if (amountFromThisSupplier > 0) {
                    orders.add(new ProductionOrderDTO(supplier.getName(), product.getName(), amountFromThisSupplier, "PLANNED"));
                    quantityNeeded -= amountFromThisSupplier;
                }
            }
            if (quantityNeeded > 0) {
                orders.add(new ProductionOrderDTO("SEM FORNECEDOR DISPONÍVEL", product.getName(), quantityNeeded, "SHORTAGE"));
            }
        }
        return orders;
    }

    @Override
    public Map<Product, Double> recalculatePlanned(Menu menu) {
        provisioningItemRepo.deleteByMenuAndType(menu.getId(), ProvisioningType.PLANNED);

        Map<ProductDTO, Double> calculated = calculatePlannedQuantities(menuMapper.toDTO(menu));

        Map<Product, Double> result = new HashMap<>();

        calculated.forEach((prodDto, qty) -> {
            Product p = productMapper.toDomain(prodDto);
            result.put(p, qty);

            // Salvar na BD para persistência
            provisioningItemRepo.save(new ProvisioningItem(
                    menu, p, qty, ProvisioningType.PLANNED, LocalDateTime.now()
            ));
        });

        return result;
    }

    private boolean hasCapacityForProduct(SupplierApplication app, String productName, LocalDate menuStartDate) {
        return app.getSupplierCapacity().stream()
                .anyMatch(cap -> cap.getProductName().equalsIgnoreCase(productName) &&
                        !menuStartDate.isBefore(cap.getStartDate()) &&
                        !menuStartDate.isAfter(cap.getEndDate()) &&
                        cap.getQuantity() > 0);
    }

    private double getSupplierAvailableCapacity(SupplierApplication app, String productName) {
        return app.getSupplierCapacity().stream()
                .filter(cap -> cap.getProductName().equalsIgnoreCase(productName))
                .mapToDouble(SupplierCapacity::getQuantity)
                .sum();
    }

    public Map<Product, Double> calculateNextWeekProductNeedsFromCurrentWeekReservations() {
        LocalDate today = LocalDate.now();

        LocalDate currentWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);

        LocalDate nextWeekStart = currentWeekStart.plusWeeks(1);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);

        Menu nextWeekMenu = menuRepo.findNextWeekMenu(nextWeekStart, nextWeekEnd)
                .orElseThrow(() ->
                        new IllegalArgumentException("No menu found for next week.")
                );

        Map<Product, Double> neededProducts = new HashMap<>();

        for (MenuEntry entry : nextWeekMenu.getEntries()) {
            for (MenuEntryDish med : entry.getMenuEntryDishes()) {
                Long dishId = med.getDish().getId();

                long currentWeekReservations =
                        reservationRepo.countConfirmedByDishBetweenDates(
                                dishId,
                                currentWeekStart.atStartOfDay(),
                                currentWeekEnd.atTime(23, 59, 59)
                        );

                for (DishIngredient di : med.getDish().getDishIngredients()) {
                    Ingredient ingredient = di.getIngredient();
                    Product product = ingredient.getProduct();

                    double totalNeeded =
                            currentWeekReservations
                                    * di.getQuantity()
                                    * ingredient.getQuantity();

                    neededProducts.merge(product, totalNeeded, Double::sum);
                }
            }
        }

        return neededProducts;
    }
}