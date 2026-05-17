package bioCanteenApp.dish.domain;

import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.recipes.domain.Recipe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DishTest {

    @Test
    void shouldCreateEmptyDish() {
        Dish dish = new Dish();

        assertNull(dish.getId());
        assertNull(dish.getDishName());
        assertNull(dish.getNutritionalInformation());
        assertNull(dish.getDishType());
        assertNull(dish.getRecipe());
        assertNotNull(dish.getDishIngredients());
        assertTrue(dish.getDishIngredients().isEmpty());
    }

    @Test
    void shouldCreateDishWithSimpleConstructor() {
        Dish dish = new Dish("Meat Dish", DishType.MEAT);

        assertEquals("Meat Dish", dish.getDishName());
        assertEquals(DishType.MEAT, dish.getDishType());
        assertNotNull(dish.getDishIngredients());
        assertTrue(dish.getDishIngredients().isEmpty());
    }

    @Test
    void shouldCreateDishWithFullConstructor() {
        Recipe recipe = new Recipe("Recipe 1", "Cook everything.");
        List<DishIngredient> ingredients = new ArrayList<>();

        Dish dish = new Dish(
                "Vegetarian Dish",
                "100 kcal",
                DishType.VEGETARIAN,
                recipe,
                ingredients
        );

        assertEquals("Vegetarian Dish", dish.getDishName());
        assertEquals("100 kcal", dish.getNutritionalInformation());
        assertEquals(DishType.VEGETARIAN, dish.getDishType());
        assertEquals(recipe, dish.getRecipe());
        assertEquals(ingredients, dish.getDishIngredients());
    }

    @Test
    void shouldSetAndGetRecipe() {
        Dish dish = new Dish();
        Recipe recipe = new Recipe("Recipe 1", "Cook everything.");

        dish.setRecipe(recipe);

        assertEquals(recipe, dish.getRecipe());
    }

    @Test
    void shouldAddIngredient() {
        Dish dish = new Dish("Fish Dish", DishType.FISH);
        Ingredient ingredient = new Ingredient("Rice", 1.0, createProduct());

        dish.addIngredient(ingredient, 2.5);

        assertEquals(1, dish.getDishIngredients().size());

        DishIngredient dishIngredient = dish.getDishIngredients().get(0);

        assertEquals(dish, dishIngredient.getDish());
        assertEquals(ingredient, dishIngredient.getIngredient());
        assertEquals(2.5, dishIngredient.getQuantity());
    }

    @Test
    void shouldGetIngredientsFromDishIngredients() {
        Dish dish = new Dish("Diet Dish", DishType.DIET);

        Ingredient ingredient1 = new Ingredient("Rice", 1.0, createProduct());
        Ingredient ingredient2 = new Ingredient("Fish", 2.0, createProduct());

        dish.addIngredient(ingredient1, 1.0);
        dish.addIngredient(ingredient2, 2.0);

        List<Ingredient> ingredients = dish.getIngredients();

        assertEquals(2, ingredients.size());
        assertTrue(ingredients.contains(ingredient1));
        assertTrue(ingredients.contains(ingredient2));
    }

    @Test
    void shouldSetAndGetId() {
        Dish dish = new Dish();

        dish.setId(1L);

        assertEquals(1L, dish.getId());
    }

    @Test
    void shouldSetAndGetDishName() {
        Dish dish = new Dish();

        dish.setDishName("Rice");

        assertEquals("Rice", dish.getDishName());
    }

    @Test
    void shouldSetAndGetNutritionalInformation() {
        Dish dish = new Dish();

        dish.setNutritionalInformation("200 kcal");

        assertEquals("200 kcal", dish.getNutritionalInformation());
    }

    @Test
    void shouldSetAndGetDishType() {
        Dish dish = new Dish();

        dish.setDishType(DishType.FISH);

        assertEquals(DishType.FISH, dish.getDishType());
    }

    @Test
    void shouldSetAndGetDishIngredients() {
        Dish dish = new Dish();
        List<DishIngredient> ingredients = new ArrayList<>();

        dish.setDishIngredients(ingredients);

        assertEquals(ingredients, dish.getDishIngredients());
    }

    private Product createProduct() {
        return new Product(
                "Rice",
                "kg",
                365,
                List.of(),
                List.of()
        );
    }
}