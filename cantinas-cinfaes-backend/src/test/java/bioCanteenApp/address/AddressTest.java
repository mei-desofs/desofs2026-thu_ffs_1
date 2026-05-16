package bioCanteenApp.address;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateAddressWithAllFields() {
        Address address = new Address(
                "Rua das Flores",
                Municipality.FELGUEIRAS,
                Village.ANSIAES,
                "Portugal",
                "4000-123"
        );

        assertEquals("Rua das Flores", address.getStreet());
        assertEquals(Municipality.FELGUEIRAS, address.getMunicipality());
        assertEquals(Village.ANSIAES, address.getVillage());
        assertEquals("Portugal", address.getCountry());
        assertEquals("4000-123", address.getPostalCode());
    }

    @Test
    void shouldCreateAddressWithEmptyConstructor() {
        Address address = new Address();

        assertNull(address.getId());
        assertNull(address.getStreet());
        assertNull(address.getMunicipality());
        assertNull(address.getVillage());
        assertNull(address.getCountry());
        assertNull(address.getPostalCode());
    }

    @Test
    void shouldSetAndGetId() {
        Address address = new Address();

        address.setId(1L);

        assertEquals(1L, address.getId());
    }

    @Test
    void shouldSetAndGetStreet() {
        Address address = new Address();

        address.setStreet("Avenida Central");

        assertEquals("Avenida Central", address.getStreet());
    }

    @Test
    void shouldSetAndGetMunicipality() {
        Address address = new Address();

        address.setMunicipality(Municipality.PENAFIEL);

        assertEquals(Municipality.PENAFIEL, address.getMunicipality());
    }

    @Test
    void shouldSetAndGetVillage() {
        Address address = new Address();

        address.setVillage(Village.FREGIM);

        assertEquals(Village.FREGIM, address.getVillage());
    }

    @Test
    void shouldSetAndGetCountry() {
        Address address = new Address();

        address.setCountry("Portugal");

        assertEquals("Portugal", address.getCountry());
    }

    @Test
    void shouldSetAndGetPostalCode() {
        Address address = new Address();

        address.setPostalCode("4100-456");

        assertEquals("4100-456", address.getPostalCode());
    }
}