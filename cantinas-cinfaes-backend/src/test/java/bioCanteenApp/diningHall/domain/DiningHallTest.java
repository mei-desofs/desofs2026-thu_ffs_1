package bioCanteenApp.diningHall.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.waste.domain.Waste;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiningHallTest {

    @Test
    void shouldCreateDiningHallWithConstructor() {
        Canteen canteen = createCanteen();

        DiningHall diningHall = new DiningHall("Main Dining Hall", canteen);

        assertEquals("Main Dining Hall", diningHall.getName());
        assertEquals(canteen, diningHall.getCanteen());
    }

    @Test
    void shouldSetAndGetId() {
        DiningHall diningHall = new DiningHall("Main Dining Hall", createCanteen());

        diningHall.setId(1L);

        assertEquals(1L, diningHall.getId());
    }

    @Test
    void shouldSetAndGetName() {
        DiningHall diningHall = new DiningHall("Old Name", createCanteen());

        diningHall.setName("New Name");

        assertEquals("New Name", diningHall.getName());
    }

    @Test
    void shouldSetAndGetCanteen() {
        DiningHall diningHall = new DiningHall("Main Dining Hall", createCanteen());
        Canteen newCanteen = createCanteen();

        diningHall.setCanteen(newCanteen);

        assertEquals(newCanteen, diningHall.getCanteen());
    }

    @Test
    void shouldSetAndGetWastes() {
        DiningHall diningHall = new DiningHall("Main Dining Hall", createCanteen());
        List<Waste> wastes = new ArrayList<>();

        diningHall.setWastes(wastes);

        assertEquals(wastes, diningHall.getWastes());
    }

    private Canteen createCanteen() {
        Address address = new Address(
                "Rua Central",
                Municipality.MARCO_DE_CANAVESES,
                Village.FORNOS,
                "Portugal",
                "4000-111"
        );

        return new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );
    }
}