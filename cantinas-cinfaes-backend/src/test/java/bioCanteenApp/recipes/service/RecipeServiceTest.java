package bioCanteenApp.recipes.service;

import bioCanteenApp.recipes.domain.Recipe;
import bioCanteenApp.recipes.dto.RecipeDTO;
import bioCanteenApp.recipes.mapper.RecipeMapper;
import bioCanteenApp.recipes.repository.RecipeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private RecipeMapper recipeMapper;
    private RecipeRepo recipeRepo;

    private RecipeService service;

    @BeforeEach
    void setUp() {
        recipeMapper = mock(RecipeMapper.class);
        recipeRepo = mock(RecipeRepo.class);

        service = new RecipeService(
                recipeMapper,
                recipeRepo
        );
    }

    @Test
    void shouldReturnEmptyRecipeList() {
        List<RecipeDTO> result =
                service.getAllRecipes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnNullWhenGettingRecipeById() {
        RecipeDTO result =
                service.getRecipeById(1L);

        assertNull(result);
    }

    @Test
    void shouldCreateRecipe() {
        RecipeDTO dto = new RecipeDTO();

        Recipe recipe = new Recipe(
                "Rice Recipe",
                "Cook the rice for 20 minutes."
        );

        when(recipeMapper.toDomain(dto))
                .thenReturn(recipe);

        when(recipeRepo.save(recipe))
                .thenReturn(recipe);

        when(recipeMapper.toDTO(recipe))
                .thenReturn(dto);

        RecipeDTO result =
                service.createRecipe(dto);

        assertEquals(dto, result);

        verify(recipeMapper).toDomain(dto);
        verify(recipeRepo).save(recipe);
        verify(recipeMapper).toDTO(recipe);
    }

    @Test
    void shouldGetRecipeCount() {
        when(recipeRepo.count())
                .thenReturn(10L);

        Long result =
                service.getRecipeCount();

        assertEquals(10L, result);

        verify(recipeRepo).count();
    }
}