package bioCanteenApp.recipes.controller;

import bioCanteenApp.recipes.dto.RecipeDTO;
import bioCanteenApp.recipes.service.IRecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Slf4j
public class RecipeController {

    private final IRecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {

        log.info("Fetching all recipes");

        List<RecipeDTO> recipes =
                recipeService.getAllRecipes();

        log.info("Found {} recipes", recipes.size());

        return ResponseEntity.ok(recipes);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(
            @PathVariable Long id
    ) {

        log.info("Fetching recipe with id: {}", id);

        RecipeDTO recipe =
                recipeService.getRecipeById(id);

        return ResponseEntity.ok(recipe);
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(
            @RequestBody RecipeDTO recipeDTO
    ) {

        log.info("Creating recipe");

        RecipeDTO createdRecipe =
                recipeService.createRecipe(recipeDTO);

        log.info(
                "Recipe created successfully with id: {}",
                createdRecipe.getId()
        );

        return ResponseEntity.ok(createdRecipe);
    }

    @GetMapping(value = "/stats")
    public ResponseEntity<Long> getRecipeCount() {

        log.info("Fetching recipe statistics");

        Long count =
                recipeService.getRecipeCount();

        log.info("Current recipe count: {}", count);

        return ResponseEntity.ok(count);
    }
}