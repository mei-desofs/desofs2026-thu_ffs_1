package bioCanteenApp.recipes.mapper;

import bioCanteenApp.recipes.domain.Recipe;
import bioCanteenApp.recipes.dto.RecipeDTO;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper implements IRecipeMapper {

    @Override
    public Recipe toDomain(RecipeDTO dto) {
        return new Recipe(
                dto.getName(),
                dto.getInstructions()
        );
    }

    @Override
    public RecipeDTO toDTO(Recipe recipe) {
        return new RecipeDTO(
                recipe.getId(),
                recipe.getName(),
                recipe.getInstructions()
        );
    }
}
