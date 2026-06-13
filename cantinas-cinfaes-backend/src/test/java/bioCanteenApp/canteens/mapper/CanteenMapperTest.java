package bioCanteenApp.canteens.mapper;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.mappers.CanteenMapper;
import bioCanteenApp.suppliers.dto.AddressDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanteenMapperTest {

    private final CanteenMapper mapper = new CanteenMapper();

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFields() {
        Address address = new Address("Rua Test", Municipality.CINFAES, Village.ANSIAES, "Portugal", "4560-000");
        Canteen canteen = new Canteen("Cantina A", address, 100, true);
        canteen.setIsQuarantined(false);

        CanteenDTO dto = mapper.toDTO(canteen);

        assertNotNull(dto);
        assertEquals("Cantina A", dto.getName());
        assertEquals(100, dto.getCapacity());
        assertTrue(dto.getCanCookDishes());
        assertFalse(dto.getIsQuarantine());
        assertNotNull(dto.getLocation());
        assertEquals("Rua Test", dto.getLocation().getStreet());
        assertEquals("CINFAES", dto.getLocation().getMunicipality());
        assertEquals("ANSIAES", dto.getLocation().getVillage());
        assertEquals("Portugal", dto.getLocation().getCountry());
        assertEquals("4560-000", dto.getLocation().getPostalCode());
    }

    @Test
    void toDTO_withNullAddressMunicipalityAndVillage_mapsNullFields() {
        Address address = new Address("Rua B", null, null, "Portugal", "0000-000");
        Canteen canteen = new Canteen("Cantina B", address, 50, false);

        CanteenDTO dto = mapper.toDTO(canteen);

        assertNull(dto.getLocation().getMunicipality());
        assertNull(dto.getLocation().getVillage());
    }

    @Test
    void toDTO_withNullAddress_mapsNullLocation() {
        Canteen canteen = new Canteen("Cantina C", null, 30, true);

        CanteenDTO dto = mapper.toDTO(canteen);

        assertNull(dto.getLocation());
    }

    @Test
    void toDomain_withNull_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_mapsAllFields() {
        AddressDTO addressDTO = AddressDTO.builder()
                .street("Rua Nova")
                .municipality("CINFAES")
                .village("ANSIAES")
                .country("Portugal")
                .postalCode("4560-001")
                .build();
        CanteenDTO dto = CanteenDTO.builder()
                .name("Cantina D")
                .location(addressDTO)
                .capacity(200)
                .canCookDishes(true)
                .isQuarantine(false)
                .build();

        Canteen canteen = mapper.toDomain(dto);

        assertNotNull(canteen);
        assertEquals("Cantina D", canteen.getName());
        assertEquals(200, canteen.getCapacity());
        assertTrue(canteen.getCanCookDishes());
        assertNotNull(canteen.getLocation());
        assertEquals(Municipality.CINFAES, canteen.getLocation().getMunicipality());
        assertEquals(Village.ANSIAES, canteen.getLocation().getVillage());
    }

    @Test
    void toDomain_withNullMunicipalityAndVillage_mapsNullEnums() {
        AddressDTO addressDTO = AddressDTO.builder()
                .street("Rua C")
                .municipality(null)
                .village(null)
                .country("Portugal")
                .postalCode("0000-000")
                .build();
        CanteenDTO dto = CanteenDTO.builder()
                .name("Cantina E")
                .location(addressDTO)
                .capacity(50)
                .canCookDishes(false)
                .build();

        Canteen canteen = mapper.toDomain(dto);

        assertNull(canteen.getLocation().getMunicipality());
        assertNull(canteen.getLocation().getVillage());
    }

    @Test
    void toDomain_withNullAddress_mapsNullLocation() {
        CanteenDTO dto = CanteenDTO.builder()
                .name("Cantina F")
                .location(null)
                .capacity(100)
                .canCookDishes(true)
                .build();

        Canteen canteen = mapper.toDomain(dto);

        assertNull(canteen.getLocation());
    }
}
