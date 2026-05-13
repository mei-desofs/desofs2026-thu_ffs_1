package bioCanteenApp.dish.mapper;


import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
import bioCanteenApp.recipes.repository.IRecipeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DishMapper implements IDishMapper {

    private final IRecipeRepo recipeRepository;
    private final IDishRepo dishRepository;
    private final IIngredientRepo ingredientRepository;

    @Override
    public Dish toDomain(DishDto dishDto) {
        if (dishDto == null) return null;

        Dish dish = (dishDto.getId() != null)
                ? dishRepository.findById(dishDto.getId()).orElse(new Dish())
                : new Dish();

        dish.setDishName(dishDto.getDishName());
        dish.setNutritionalInformation(dishDto.getNutritionalInformation());

        if (dishDto.getDishType() != null) {
            dish.setDishType(DishType.valueOf(dishDto.getDishType()));
        }

        if (dishDto.getRecipeId() != null) {
            dish.setRecipe(recipeRepository.findById(dishDto.getRecipeId()).orElse(null));
        }

        if (dishDto.getIngredients() != null) {
            dish.getDishIngredients().clear();

            for (DishIngredientDto diDto : dishDto.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(diDto.getIngredientId())
                        .orElseThrow(() -> new RuntimeException("Ingredient not found: " + diDto.getIngredientId()));

                dish.addIngredient(ingredient, diDto.getQuantity());
            }
        }

        return dish;
    }

    @Override
    public DishDto toDTO(Dish dish) {
        if (dish == null) return null;

        var ingredientDtos = dish.getDishIngredients().stream()
                .map(di -> new DishIngredientDto(
                        di.getIngredient().getId(),
                        di.getIngredient().getProduct().getId(),
                        di.getIngredient().getName(),
                        di.getQuantity(),
                        di.getIngredient().getProduct().getUnit()
                ))
                .collect(Collectors.toList());

        return new DishDto(
                dish.getId(),
                dish.getDishName(),
                dish.getNutritionalInformation(),
                dish.getDishType() != null ? dish.getDishType().name() : null,
                dish.getRecipe() != null ? dish.getRecipe().getId() : null,
                ingredientDtos
        );
    }
}
