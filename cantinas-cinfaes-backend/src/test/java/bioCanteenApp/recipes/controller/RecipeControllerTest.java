package bioCanteenApp.recipes.controller;

import bioCanteenApp.recipes.dto.RecipeDTO;
import bioCanteenApp.recipes.service.IRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeControllerTest {

    private IRecipeService recipeService;
    private RecipeController controller;

    @BeforeEach
    void setUp() {
        recipeService = mock(IRecipeService.class);

        controller = new RecipeController(recipeService);
    }

    @Test
    void shouldGetAllRecipes() {
        List<RecipeDTO> recipes = List.of(
                new RecipeDTO(),
                new RecipeDTO()
        );

        when(recipeService.getAllRecipes())
                .thenReturn(recipes);

        ResponseEntity<List<RecipeDTO>> response =
                controller.getAllRecipes();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(recipes, response.getBody());

        verify(recipeService).getAllRecipes();
    }

    @Test
    void shouldGetRecipeById() {
        RecipeDTO dto = new RecipeDTO();

        when(recipeService.getRecipeById(1L))
                .thenReturn(dto);

        ResponseEntity<RecipeDTO> response =
                controller.getRecipeById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(recipeService).getRecipeById(1L);
    }

    @Test
    void shouldCreateRecipe() {
        RecipeDTO dto = new RecipeDTO();

        when(recipeService.createRecipe(dto))
                .thenReturn(dto);

        ResponseEntity<RecipeDTO> response =
                controller.createRecipe(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(recipeService).createRecipe(dto);
    }

    @Test
    void shouldGetRecipeCount() {
        when(recipeService.getRecipeCount())
                .thenReturn(15L);

        ResponseEntity<Long> response =
                controller.getRecipeCount();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(15L, response.getBody());

        verify(recipeService).getRecipeCount();
    }
}