package bioCanteenApp.dish.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.mapper.DishMapper;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.repository.MenuRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.repository.IProductRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DishService implements IDishService {

    private final IDishRepo dishRepo;
    private final IIngredientRepo ingredientRepo;
    private final IProductRepo productRepo;
    private final DishMapper dishMapper;
    private final MenuRepo menuRepository;
    private final IProductRepo productBatchRepo;

    @Override
    public GetDishDTO generateDishInformation(GetDishDTO dto) {
        Set<Allergen> detectedAllergens = new HashSet<>();
        List<String> translatedAllergens = new ArrayList<>();

        for (IngredientDto ing : dto.getIngredients()) {
            Ingredient ingredient = ingredientRepo.findByName(ing.getName()).getFirst();

            detectedAllergens.addAll(ingredient.getProduct().getAllergens());
        }

        for (Allergen allergen : detectedAllergens) {
            translatedAllergens.add(allergen.getPtLabel());
        }

        dto.setAllergens(translatedAllergens);
        dto.setNutritionalInformation(generateNutritionalInfo(detectedAllergens));

        return dto;
    }

    private String generateNutritionalInfo(Set<Allergen> allergens) {

        List<String> parts = new ArrayList<>();

        if (allergens.contains(Allergen.FISH)) {
            parts.add("rica em ómega-3");
        }

        if (allergens.contains(Allergen.MILK) || allergens.contains(Allergen.EGGS)) {
            parts.add("boa fonte de proteína e cálcio");
        }

        if (allergens.contains(Allergen.GLUTEN)) {
            parts.add("com hidratos de carbono essenciais para energia");
        }

        if (allergens.contains(Allergen.NUTS)) {
            parts.add("contém gorduras saudáveis");
        }

        if (parts.isEmpty()) {
            return "Refeição leve e equilibrada, adequada a uma dieta variada.";
        }

        return "Refeição equilibrada, " + String.join(", ", parts) + ".";
    }

    @Override
    public List<DishDto> getAll() {
        List<Dish> dishes = dishRepo.findAll();
        return dishes.stream().map(dishMapper::toDTO).toList();
    }

    @Override
    public List<DishDto> getDishesWithSeasonalIngredients(List<String> seasonalProductNames) {
        List<Dish> dishes = dishRepo.findAll();

        return dishes.stream()
                .filter(dish -> dish.getIngredients().stream()
                        .allMatch(ing ->
                                seasonalProductNames.contains(ing.getProduct().getName())
                        )
                )
                .map(dishMapper::toDTO)
                .toList();
    }

    public List<DishDto> getAlternatives(Long menuEntryDishId) {
        MenuEntryDish med = menuRepository.findMenuEntryDishById(menuEntryDishId);
        if (med == null) throw new IllegalArgumentException("MenuEntryDish não encontrado: " + menuEntryDishId);

        if (med.getDish() == null)
            throw new IllegalStateException("MenuEntryDish não tem Dish associado: " + menuEntryDishId);
        if (med.getMenuEntry() == null || med.getMenuEntry().getDate() == null)
            throw new IllegalStateException("MenuEntry ou Data é null para MenuEntryDish: " + menuEntryDishId);

        Season season = Season.fromMonth(med.getMenuEntry().getDate().getMonth());

        return dishRepo.findSeasonalAlternatives(
                        med.getDish().getDishType(),
                        season,
                        med.getDish().getId()
                ).stream()
                .map(dishMapper::toDTO)
                .filter(this::hasStockForDish)
                .toList();
    }

    private boolean hasStockForDish(DishDto dishDto) {
        if (dishDto == null || dishDto.getIngredients() == null || dishDto.getIngredients().isEmpty()) {
            return false;
        }

        for (DishIngredientDto ingredient : dishDto.getIngredients()) {
            Long productId = ingredient.getProductId();
            double requiredQuantity = ingredient.getQuantity();

            double availableQuantity = productBatchRepo.sumValidStockByProduct(productId);

            if (availableQuantity < requiredQuantity) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void replaceDish(Long oldDishId, Long newDishDto) {
        MenuEntryDish med = menuRepository.findMenuEntryDishById(oldDishId);
        if (med == null) {
            throw new IllegalArgumentException("MenuEntryDish não encontrado: " + oldDishId);
        }

        Dish newDish = dishRepo.findById(newDishDto)
                .orElseThrow(() ->
                        new IllegalArgumentException("Dish não encontrado: " + newDishDto)
                );

        med.setDish(newDish);
    }

    @Override
    public List<DishType> getDishType() {
        return Arrays.asList(DishType.values());
    }

    @Override
    public DishDto createDish(DishDto dto) {
        Dish dish = dishMapper.toDomain(dto);
        Dish savedDish = dishRepo.save(dish);
        return dishMapper.toDTO(savedDish);
    }

    @Override
    public Double getOrganicProducts() {
        return productRepo.getOrganicProducts();
    }
}
