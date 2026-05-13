package bioCanteenApp.recipes.service;


import bioCanteenApp.recipes.dto.RecipeDTO;

import java.util.List;

public interface IRecipeService {
    List<RecipeDTO> getAllRecipes();
    RecipeDTO getRecipeById(Long id);
    RecipeDTO createRecipe(RecipeDTO recipeDTO);
    Long getRecipeCount();
}
