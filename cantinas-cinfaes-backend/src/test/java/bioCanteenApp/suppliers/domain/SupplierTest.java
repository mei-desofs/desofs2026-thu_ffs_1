package bioCanteenApp.suppliers.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    @Test
    void shouldCreateSupplierWithConstructor() {
        User user = new User("supplier@email.com", "Supplier", "password", Role.USER);
        Address address = new Address("Rua Central", null, null, "Portugal", "4000-111");
        byte[] certificate = {1, 2, 3};

        SupplierApplication application = new SupplierApplication(
                "Supplier",
                "supplier@email.com",
                "912345678",
                address,
                certificate,
                123456789L,
                List.of(),
                LocalDate.of(2026, 5, 14)
        );

        Supplier supplier = new Supplier(
                user,
                "123456789",
                address,
                "912345678",
                certificate,
                application
        );

        assertEquals(user, supplier.getUser());
        assertEquals("123456789", supplier.getNif());
        assertEquals(address, supplier.getAddress());
        assertEquals("912345678", supplier.getPhoneNumber());
        assertArrayEquals(certificate, supplier.getCertifiedOrganic());
        assertEquals(application, supplier.getApplicationId());
        assertFalse(supplier.isQuarantined());
    }

    @Test
    void shouldSetAndGetId() {
        Supplier supplier = new Supplier();

        supplier.setId(1L);

        assertEquals(1L, supplier.getId());
    }

    @Test
    void shouldSetAndGetUser() {
        Supplier supplier = new Supplier();
        User user = new User("user@email.com", "User", "password", Role.USER);

        supplier.setUser(user);

        assertEquals(user, supplier.getUser());
    }

    @Test
    void shouldSetAndGetNif() {
        Supplier supplier = new Supplier();

        supplier.setNif("987654321");

        assertEquals("987654321", supplier.getNif());
    }

    @Test
    void shouldSetAndGetAddress() {
        Supplier supplier = new Supplier();
        Address address = new Address("Rua Nova", null, null, "Portugal", "4000-222");

        supplier.setAddress(address);

        assertEquals(address, supplier.getAddress());
    }

    @Test
    void shouldSetAndGetPhoneNumber() {
        Supplier supplier = new Supplier();

        supplier.setPhoneNumber("923456789");

        assertEquals("923456789", supplier.getPhoneNumber());
    }

    @Test
    void shouldSetAndGetCertifiedOrganic() {
        Supplier supplier = new Supplier();
        byte[] certificate = {4, 5, 6};

        supplier.setCertifiedOrganic(certificate);

        assertArrayEquals(certificate, supplier.getCertifiedOrganic());
    }

    @Test
    void shouldSetAndGetApplicationId() {
        Supplier supplier = new Supplier();
        Address address = new Address("Rua Central", null, null, "Portugal", "4000-111");

        SupplierApplication application = new SupplierApplication(
                "Supplier",
                "supplier@email.com",
                "912345678",
                address,
                new byte[]{1, 2, 3},
                123456789L,
                List.of(),
                LocalDate.of(2026, 5, 14)
        );

        supplier.setApplicationId(application);

        assertEquals(application, supplier.getApplicationId());
    }

    @Test
    void shouldSetAndGetIsQuarantined() {
        Supplier supplier = new Supplier();

        supplier.setQuarantined(true);

        assertTrue(supplier.isQuarantined());
    }

    @Test
    void shouldSetQuarantinedToFalseOnCreate() {
        Supplier supplier = new Supplier();
        supplier.setQuarantined(true);

        supplier.onCreate();

        assertFalse(supplier.isQuarantined());
    }
}