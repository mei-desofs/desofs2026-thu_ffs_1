package bioCanteenApp.recipes.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.recipes.domain.Recipe;
import bioCanteenApp.recipes.dto.RecipeDTO;
import bioCanteenApp.recipes.mapper.RecipeMapper;
import bioCanteenApp.recipes.repository.RecipeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService implements IRecipeService {

    private final RecipeMapper recipeMapper;
    private final RecipeRepo recipeRepo;

    @Override
    public List<RecipeDTO> getAllRecipes() {
        return List.of();
    }

    @Override
    public RecipeDTO getRecipeById(Long id) {
        return null;
    }

    @Override
    public RecipeDTO createRecipe(RecipeDTO recipeDTO) {
        Recipe recipe = recipeMapper.toDomain(recipeDTO);
        Recipe savedRecipe = recipeRepo.save(recipe);
        return recipeMapper.toDTO(savedRecipe);
    }

    @Override
    public Long getRecipeCount() {
        return recipeRepo.count();
    }
}
