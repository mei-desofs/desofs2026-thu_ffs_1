package bioCanteenApp.ingredients.controller;

import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.service.IngredientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngredientControllerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IngredientService ingredientsService;
    private IngredientController controller;

    @BeforeEach
    void setUp() {
        ingredientsService = mock(IngredientService.class);

        controller = new IngredientController(ingredientsService);
    }

    @Test
    void shouldGetSeasonalIngredients() {
        List<IngredientDto> ingredients = List.of(
                new IngredientDto(),
                new IngredientDto()
        );

        when(ingredientsService.getSeasonalIngredients())
                .thenReturn(ingredients);

        List<IngredientDto> result =
                controller.getSeasonalIngredients();

        assertEquals(ingredients, result);

        verify(ingredientsService).getSeasonalIngredients();
    }

    @Test
    void shouldGetAllIngredients() {
        List<IngredientDto> ingredients = List.of(
                new IngredientDto(),
                new IngredientDto()
        );

        when(ingredientsService.getAllIngredients())
                .thenReturn(ingredients);

        ResponseEntity<List<IngredientDto>> response =
                controller.getAllIngredients();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ingredients, response.getBody());

        verify(ingredientsService).getAllIngredients();
    }

    @Test
    void shouldGetIngredientCount() {
        when(ingredientsService.getIngredientCount())
                .thenReturn(25L);

        ResponseEntity<Long> response =
                controller.getIngredientCount();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(25L, response.getBody());

        verify(ingredientsService).getIngredientCount();
    }
}