package bioCanteenApp.products.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductBatchTest {

    @Test
    void shouldCreateProductBatchWithConstructor() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        LocalDate receivedDate = LocalDate.of(2026, 5, 14);

        ProductBatch productBatch = new ProductBatch(
                product,
                10.0,
                receivedDate,
                true,
                null
        );

        assertEquals(product, productBatch.getProduct());
        assertEquals(10.0, productBatch.getQuantity());
        assertEquals(receivedDate, productBatch.getReceivedDate());
        assertEquals(receivedDate.plusDays(365), productBatch.getExpirationDate());
        assertTrue(productBatch.isBio());
        assertFalse(productBatch.isQuarantined());
        assertNull(productBatch.getSupplier());
    }

    @Test
    void shouldSetAndGetId() {
        ProductBatch productBatch = new ProductBatch();

        productBatch.setId(1L);

        assertEquals(1L, productBatch.getId());
    }

    @Test
    void shouldSetAndGetProduct() {
        ProductBatch productBatch = new ProductBatch();

        Product product = new Product(
                "Fish",
                "kg",
                5,
                List.of(Season.SUMMER),
                List.of(Allergen.FISH)
        );

        productBatch.setProduct(product);

        assertEquals(product, productBatch.getProduct());
    }

    @Test
    void shouldSetAndGetQuantity() {
        ProductBatch productBatch = new ProductBatch();

        productBatch.setQuantity(25.5);

        assertEquals(25.5, productBatch.getQuantity());
    }

    @Test
    void shouldSetAndGetReceivedDate() {
        ProductBatch productBatch = new ProductBatch();

        LocalDate receivedDate = LocalDate.of(2026, 5, 14);
        productBatch.setReceivedDate(receivedDate);

        assertEquals(receivedDate, productBatch.getReceivedDate());
    }

    @Test
    void shouldSetAndGetExpirationDate() {
        ProductBatch productBatch = new ProductBatch();

        LocalDate expirationDate = LocalDate.of(2026, 6, 14);
        productBatch.setExpirationDate(expirationDate);

        assertEquals(expirationDate, productBatch.getExpirationDate());
    }

    @Test
    void shouldSetAndGetIsBio() {
        ProductBatch productBatch = new ProductBatch();

        productBatch.setBio(true);

        assertTrue(productBatch.isBio());
    }

    @Test
    void shouldSetAndGetIsQuarantined() {
        ProductBatch productBatch = new ProductBatch();

        productBatch.setQuarantined(true);

        assertTrue(productBatch.isQuarantined());
    }

    @Test
    void shouldSetAndGetSupplier() {
        ProductBatch productBatch = new ProductBatch();

        productBatch.setSupplier(null);

        assertNull(productBatch.getSupplier());
    }
}