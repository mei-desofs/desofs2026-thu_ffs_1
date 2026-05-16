package bioCanteenApp.ingredients.domain;

import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void shouldCreateIngredientWithConstructor() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING, Season.SUMMER),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        assertEquals("Rice", ingredient.getName());
        assertEquals(1.0, ingredient.getQuantity());
        assertEquals(product, ingredient.getProduct());
    }

    @Test
    void shouldSetAndGetId() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        ingredient.setId(1L);

        assertEquals(1L, ingredient.getId());
    }

    @Test
    void shouldSetAndGetName() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        ingredient.setName("Potato");

        assertEquals("Potato", ingredient.getName());
    }

    @Test
    void shouldSetAndGetQuantity() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        ingredient.setQuantity(2.5);

        assertEquals(2.5, ingredient.getQuantity());
    }

    @Test
    void shouldSetAndGetProduct() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Product newProduct = new Product(
                "Fish",
                "kg",
                5,
                List.of(Season.SUMMER),
                List.of(Allergen.FISH)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        ingredient.setProduct(newProduct);

        assertEquals(newProduct, ingredient.getProduct());
    }
}