package bioCanteenApp.suppliers.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SupplierCapacityTest {

    @Test
    void shouldCreateSupplierCapacityWithConstructor() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        SupplierCapacity capacity = new SupplierCapacity(
                "Rice",
                startDate,
                endDate,
                1000.0
        );

        assertEquals("Rice", capacity.getProductName());
        assertEquals(startDate, capacity.getStartDate());
        assertEquals(endDate, capacity.getEndDate());
        assertEquals(1000.0, capacity.getQuantity());
        assertNull(capacity.getRemainingQuantity());
    }

    @Test
    void shouldCreateEmptySupplierCapacity() {
        SupplierCapacity capacity = new SupplierCapacity();

        assertNull(capacity.getProductName());
        assertNull(capacity.getStartDate());
        assertNull(capacity.getEndDate());
        assertNull(capacity.getQuantity());
        assertNull(capacity.getRemainingQuantity());
    }

    @Test
    void shouldSetAndGetProductName() {
        SupplierCapacity capacity = new SupplierCapacity();

        capacity.setProductName("Potato");

        assertEquals("Potato", capacity.getProductName());
    }

    @Test
    void shouldSetAndGetStartDate() {
        SupplierCapacity capacity = new SupplierCapacity();
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        capacity.setStartDate(startDate);

        assertEquals(startDate, capacity.getStartDate());
    }

    @Test
    void shouldSetAndGetEndDate() {
        SupplierCapacity capacity = new SupplierCapacity();
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        capacity.setEndDate(endDate);

        assertEquals(endDate, capacity.getEndDate());
    }

    @Test
    void shouldSetAndGetQuantity() {
        SupplierCapacity capacity = new SupplierCapacity();

        capacity.setQuantity(500.0);

        assertEquals(500.0, capacity.getQuantity());
    }

    @Test
    void shouldSetAndGetRemainingQuantity() {
        SupplierCapacity capacity = new SupplierCapacity();

        capacity.setRemainingQuantity(250.0);

        assertEquals(250.0, capacity.getRemainingQuantity());
    }
}