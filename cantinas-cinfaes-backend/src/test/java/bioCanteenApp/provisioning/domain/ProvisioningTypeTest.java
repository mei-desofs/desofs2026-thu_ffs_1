package bioCanteenApp.provisioning.domain;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProvisioningTypeTest {

    @Test
    void shouldCreateProvisioningItemWithConstructor() {
        Menu menu = new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                MenuStatus.GENERATED
        );

        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 14, 10, 30);

        ProvisioningItem item = new ProvisioningItem(
                menu,
                product,
                10.0,
                ProvisioningType.ADJUSTED,
                createdAt
        );

        assertEquals(menu, item.getMenu());
        assertEquals(product, item.getProduct());
        assertEquals(10.0, item.getQuantity());
        assertEquals(ProvisioningType.ADJUSTED, item.getType());
        assertEquals(createdAt, item.getCreatedAt());
    }

    @Test
    void shouldSetAndGetId() {
        ProvisioningItem item = new ProvisioningItem();

        item.setId(1L);

        assertEquals(1L, item.getId());
    }

    @Test
    void shouldSetAndGetMenu() {
        ProvisioningItem item = new ProvisioningItem();

        Menu menu = new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                MenuStatus.GENERATED
        );

        item.setMenu(menu);

        assertEquals(menu, item.getMenu());
    }

    @Test
    void shouldSetAndGetProduct() {
        ProvisioningItem item = new ProvisioningItem();

        Product product = new Product(
                "Potato",
                "kg",
                30,
                List.of(Season.WINTER),
                List.of()
        );

        item.setProduct(product);

        assertEquals(product, item.getProduct());
    }

    @Test
    void shouldSetAndGetQuantity() {
        ProvisioningItem item = new ProvisioningItem();

        item.setQuantity(20.5);

        assertEquals(20.5, item.getQuantity());
    }

    @Test
    void shouldSetAndGetType() {
        ProvisioningItem item = new ProvisioningItem();

        item.setType(ProvisioningType.ADJUSTED);

        assertEquals(ProvisioningType.ADJUSTED, item.getType());
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        ProvisioningItem item = new ProvisioningItem();
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 14, 10, 30);

        item.setCreatedAt(createdAt);

        assertEquals(createdAt, item.getCreatedAt());
    }
}