package bioCanteenApp.recipes.mapper;

import bioCanteenApp.recipes.domain.Recipe;
import bioCanteenApp.recipes.dto.RecipeDTO;

public interface IRecipeMapper {
    Recipe toDomain(RecipeDTO dto);
    RecipeDTO toDTO(Recipe recipe);
}
