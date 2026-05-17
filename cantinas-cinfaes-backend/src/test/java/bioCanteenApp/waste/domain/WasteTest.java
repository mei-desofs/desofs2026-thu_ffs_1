package bioCanteenApp.waste.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.suppliers.domain.Supplier;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WasteTest {

    @Test
    void shouldCreateWasteWithBasicConstructor() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        assertEquals(100.0, waste.getTotalMealsReserved());
        assertEquals(10.0, waste.getMealsNotServed());
        assertEquals(5.0, waste.getServedWaste());
        assertEquals(80.0, waste.getServedTotal());
    }

    @Test
    void shouldCreateWasteWithFullConstructor() {
        Address address = new Address(
                "Rua Central",
                Municipality.CASTELO_DE_PAIVA,
                Village.CANDEMIL,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen("ISEP Canteen", address, 300, true);
        DiningHall diningHall = new DiningHall("Main Hall", canteen);
        Supplier supplier = null;

        LocalDate date = LocalDate.of(2026, 5, 14);

        Waste waste = new Waste(
                date,
                100.0,
                10.0,
                5.0,
                80.0,
                canteen,
                diningHall,
                supplier
        );

        assertEquals(date, waste.getDate());
        assertEquals(100.0, waste.getTotalMealsReserved());
        assertEquals(10.0, waste.getMealsNotServed());
        assertEquals(5.0, waste.getServedWaste());
        assertEquals(80.0, waste.getServedTotal());
        assertEquals(canteen, waste.getCanteen());
        assertEquals(diningHall, waste.getDiningHall());
        assertNull(waste.getSupplier());
    }

    @Test
    void shouldSetAndGetId() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setId(1L);

        assertEquals(1L, waste.getId());
    }

    @Test
    void shouldSetAndGetDate() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);
        LocalDate date = LocalDate.of(2026, 5, 14);

        waste.setDate(date);

        assertEquals(date, waste.getDate());
    }

    @Test
    void shouldSetAndGetTotalMealsReserved() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setTotalMealsReserved(150.0);

        assertEquals(150.0, waste.getTotalMealsReserved());
    }

    @Test
    void shouldSetAndGetMealsNotServed() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setMealsNotServed(20.0);

        assertEquals(20.0, waste.getMealsNotServed());
    }

    @Test
    void shouldSetAndGetServedWaste() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setServedWaste(7.5);

        assertEquals(7.5, waste.getServedWaste());
    }

    @Test
    void shouldSetAndGetServedTotal() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setServedTotal(90.0);

        assertEquals(90.0, waste.getServedTotal());
    }

    @Test
    void shouldSetAndGetCanteen() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        Address address = new Address(
                "Rua Central",
                Municipality.FELGUEIRAS,
                Village.LOIVOS_DO_MONTE,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen("ISEP Canteen", address, 300, true);

        waste.setCanteen(canteen);

        assertEquals(canteen, waste.getCanteen());
    }

    @Test
    void shouldSetAndGetDiningHall() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        Address address = new Address(
                "Rua Central",
                Municipality.CINFAES,
                Village.FRENDE,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen("ISEP Canteen", address, 300, true);
        DiningHall diningHall = new DiningHall("Main Hall", canteen);

        waste.setDiningHall(diningHall);

        assertEquals(diningHall, waste.getDiningHall());
    }

    @Test
    void shouldSetAndGetSupplier() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);

        waste.setSupplier(null);

        assertNull(waste.getSupplier());
    }
}