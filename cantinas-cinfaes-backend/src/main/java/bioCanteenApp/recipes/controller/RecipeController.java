package bioCanteenApp.recipes.controller;

import bioCanteenApp.recipes.dto.RecipeDTO;
import bioCanteenApp.recipes.service.IRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {
    private final IRecipeService recipeService;

    // GET All Recipes
    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    // GET Recipe by ID
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        RecipeDTO recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(@RequestBody RecipeDTO recipeDTO) {
        RecipeDTO createdRecipe = recipeService.createRecipe(recipeDTO);
        return ResponseEntity.ok(createdRecipe);
    }

    @GetMapping("/stats")
    public ResponseEntity<Long> getRecipeCount() {
        Long count = recipeService.getRecipeCount();
        return ResponseEntity.ok(count);
    }
}
