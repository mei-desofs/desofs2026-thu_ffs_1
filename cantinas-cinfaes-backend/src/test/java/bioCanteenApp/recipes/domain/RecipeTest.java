package bioCanteenApp.recipes.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void shouldCreateRecipeWithConstructor() {
        Recipe recipe = new Recipe(
                "Pasta Recipe",
                "Boil water and cook pasta."
        );

        assertEquals("Pasta Recipe", recipe.getName());
        assertEquals("Boil water and cook pasta.", recipe.getInstructions());
    }

    @Test
    void shouldCreateRecipeWithIdConstructor() {
        Recipe recipe = new Recipe(1L);

        assertEquals(1L, recipe.getId());
    }

    @Test
    void shouldSetAndGetId() {
        Recipe recipe = new Recipe(1L);

        recipe.setId(2L);

        assertEquals(2L, recipe.getId());
    }

    @Test
    void shouldSetAndGetName() {
        Recipe recipe = new Recipe(
                "Old Recipe",
                "Old Instructions"
        );

        recipe.setName("New Recipe");

        assertEquals("New Recipe", recipe.getName());
    }

    @Test
    void shouldSetAndGetInstructions() {
        Recipe recipe = new Recipe(
                "Recipe",
                "Old Instructions"
        );

        recipe.setInstructions("New Instructions");

        assertEquals("New Instructions", recipe.getInstructions());
    }
}