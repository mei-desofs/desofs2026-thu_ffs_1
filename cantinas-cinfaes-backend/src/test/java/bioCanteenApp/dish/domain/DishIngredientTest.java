package bioCanteenApp.dish.domain;

import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DishIngredientTest {

    @Test
    void shouldSetAndGetId() {
        DishIngredient dishIngredient = new DishIngredient();

        dishIngredient.setId(1L);

        assertEquals(1L, dishIngredient.getId());
    }

    @Test
    void shouldSetAndGetDish() {
        DishIngredient dishIngredient = new DishIngredient();
        Dish dish = new Dish("Kosher Dish", DishType.KOSHER);

        dishIngredient.setDish(dish);

        assertEquals(dish, dishIngredient.getDish());
    }

    @Test
    void shouldSetAndGetIngredient() {
        DishIngredient dishIngredient = new DishIngredient();

        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING, Season.SUMMER),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient(
                "Rice",
                1.0,
                product
        );

        dishIngredient.setIngredient(ingredient);

        assertEquals(ingredient, dishIngredient.getIngredient());
    }

    @Test
    void shouldSetAndGetQuantity() {
        DishIngredient dishIngredient = new DishIngredient();

        dishIngredient.setQuantity(2.5);

        assertEquals(2.5, dishIngredient.getQuantity());
    }
}