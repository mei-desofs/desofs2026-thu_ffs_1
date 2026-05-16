package bioCanteenApp.products.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithConstructor() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING, Season.SUMMER),
                List.of(Allergen.GLUTEN)
        );

        assertEquals("Rice", product.getName());
        assertEquals("kg", product.getUnit());
        assertEquals(365, product.getExpirationDays());
        assertEquals(List.of(Season.SPRING, Season.SUMMER), product.getSeasons());
        assertEquals(List.of(Allergen.GLUTEN), product.getAllergens());
    }

    @Test
    void shouldCreateProductWithIdConstructor() {
        Product product = new Product(1L);

        assertEquals(1L, product.getId());
    }

    @Test
    void shouldSetAndGetId() {
        Product product = new Product(1L);

        product.setId(2L);

        assertEquals(2L, product.getId());
    }

    @Test
    void shouldSetAndGetName() {
        Product product = new Product(1L);

        product.setName("Potato");

        assertEquals("Potato", product.getName());
    }

    @Test
    void shouldSetAndGetUnit() {
        Product product = new Product(1L);

        product.setUnit("g");

        assertEquals("g", product.getUnit());
    }

    @Test
    void shouldSetAndGetExpirationDays() {
        Product product = new Product(1L);

        product.setExpirationDays(30);

        assertEquals(30, product.getExpirationDays());
    }

    @Test
    void shouldSetAndGetSeasons() {
        Product product = new Product(1L);

        List<Season> seasons = List.of(Season.WINTER, Season.AUTUMN);
        product.setSeasons(seasons);

        assertEquals(seasons, product.getSeasons());
    }

    @Test
    void shouldSetAndGetAllergens() {
        Product product = new Product(1L);

        List<Allergen> allergens = List.of(Allergen.FISH, Allergen.MILK);
        product.setAllergens(allergens);

        assertEquals(allergens, product.getAllergens());
    }
}