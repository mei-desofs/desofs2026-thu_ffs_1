package bioCanteenApp.canteens.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.diningHall.domain.DiningHall;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CanteenTest {

    @Test
    void shouldCreateCanteenWithConstructor() {

        Address address = new Address(
                "Rua Central",
                Municipality.PAÇOS_DE_FERREIRA,
                Village.LOMBA,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        assertEquals("ISEP Canteen", canteen.getName());
        assertEquals(address, canteen.getLocation());
        assertEquals(300, canteen.getCapacity());
        assertTrue(canteen.getCanCookDishes());
        assertFalse(canteen.getIsQuarantined());
    }

    @Test
    void shouldSetAndGetId() {

        Address address = new Address(
                "Rua Central",
                Municipality.LOUSADA,
                Village.TRAVANCA,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        canteen.setId(1L);

        assertEquals(1L, canteen.getId());
    }

    @Test
    void shouldSetAndGetName() {

        Address address = new Address(
                "Rua Central",
                Municipality.CASTELO_DE_PAIVA,
                Village.FRENDE,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "Old Name",
                address,
                300,
                true
        );

        canteen.setName("New Name");

        assertEquals("New Name", canteen.getName());
    }

    @Test
    void shouldSetAndGetLocation() {

        Address oldAddress = new Address(
                "Rua A",
                Municipality.RESENDE,
                Village.GRILO,
                "Portugal",
                "4000-111"
        );

        Address newAddress = new Address(
                "Rua B",
                Municipality.RESENDE,
                Village.GRILO,
                "Portugal",
                "4700-222"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                oldAddress,
                300,
                true
        );

        canteen.setLocation(newAddress);

        assertEquals(newAddress, canteen.getLocation());
    }

    @Test
    void shouldSetAndGetCapacity() {

        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.PEDORIDO,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        canteen.setCapacity(500);

        assertEquals(500, canteen.getCapacity());
    }

    @Test
    void shouldSetAndGetCanCookDishes() {

        Address address = new Address(
                "Rua Central",
                Municipality.BAIAO,
                Village.PEDORIDO,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        canteen.setCanCookDishes(false);

        assertFalse(canteen.getCanCookDishes());
    }

    @Test
    void shouldSetAndGetDiningHalls() {

        Address address = new Address(
                "Rua Central",
                Municipality.BAIAO,
                Village.PEDORIDO,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        List<DiningHall> diningHalls = new ArrayList<>();

        canteen.setDiningHalls(diningHalls);

        assertEquals(diningHalls, canteen.getDiningHalls());
    }

    @Test
    void shouldSetAndGetIsQuarantined() {

        Address address = new Address(
                "Rua Central",
                Municipality.CELORICO_DE_BASTO,
                Village.PEDORIDO,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        canteen.setIsQuarantined(true);

        assertTrue(canteen.getIsQuarantined());
    }
}