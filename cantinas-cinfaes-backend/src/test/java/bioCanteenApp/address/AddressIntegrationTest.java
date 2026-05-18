package bioCanteenApp.address;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AddressIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveAddress() {
        Address address = new Address(
                "Rua Principal",
                Municipality.AMARANTE,
                Village.ANSIAES,
                "Portugal",
                "4690-000"
        );

        Address savedAddress = entityManager.persistAndFlush(address);
        Address retrievedAddress = entityManager.find(Address.class, savedAddress.getId());

        assertThat(retrievedAddress).isNotNull();
        assertThat(retrievedAddress.getId()).isGreaterThan(0);
        assertThat(retrievedAddress.getStreet()).isEqualTo("Rua Principal");
        assertThat(retrievedAddress.getMunicipality()).isEqualTo(Municipality.AMARANTE);
        assertThat(retrievedAddress.getCountry()).isEqualTo("Portugal");
        assertThat(retrievedAddress.getPostalCode()).isEqualTo("4690-000");
    }
}
