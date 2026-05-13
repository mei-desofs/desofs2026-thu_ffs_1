package bioCanteenApp.dish.dto;

import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.recipes.domain.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDto {
    private Long id;
    private String dishName;
    private String nutritionalInformation;
    private String dishType;
    private Long recipeId;
    private List<DishIngredientDto> ingredients;
}