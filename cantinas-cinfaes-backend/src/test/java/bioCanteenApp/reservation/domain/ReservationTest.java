package bioCanteenApp.reservation.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void shouldCreateEmptyReservation() {
        Reservation reservation = new Reservation();

        assertNull(reservation.getId());
        assertNull(reservation.getUser());
        assertNull(reservation.getMenuEntryDish());
        assertNull(reservation.getReservationDateTime());
        assertNull(reservation.getStatus());
    }

    @Test
    void shouldCreateReservationWithConstructor() {

        Address address = new Address(
                "Rua Central",
                Municipality.CINFAES,
                Village.FORNOS,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        DiningHall diningHall = new DiningHall(
                "Main Hall",
                canteen
        );

        User user = new User(
                "user@email.com",
                "John",
                "password",
                Role.USER,
                canteen,
                diningHall
        );

        MenuEntry menuEntry = new MenuEntry();

        Dish dish = new Dish(
                "Fish Dish",
                DishType.FISH
        );

        MenuEntryDish menuEntryDish = new MenuEntryDish(
                menuEntry,
                dish
        );

        LocalDateTime reservationDateTime =
                LocalDateTime.of(2026, 5, 14, 12, 30);

        Reservation reservation = new Reservation(
                user,
                menuEntryDish,
                reservationDateTime,
                ReservationStatus.PENDING
        );

        assertEquals(user, reservation.getUser());
        assertEquals(menuEntryDish, reservation.getMenuEntryDish());
        assertEquals(reservationDateTime, reservation.getReservationDateTime());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    void shouldSetAndGetId() {
        Reservation reservation = new Reservation();

        reservation.setId(1L);

        assertEquals(1L, reservation.getId());
    }

    @Test
    void shouldSetAndGetUser() {

        Address address = new Address(
                "Rua Central",
                Municipality.AMARANTE,
                Village.FREGIM,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        DiningHall diningHall = new DiningHall(
                "Main Hall",
                canteen
        );

        User user = new User(
                "user@email.com",
                "John",
                "password",
                Role.USER,
                canteen,
                diningHall
        );

        Reservation reservation = new Reservation();

        reservation.setUser(user);

        assertEquals(user, reservation.getUser());
    }

    @Test
    void shouldSetAndGetMenuEntryDish() {

        Reservation reservation = new Reservation();

        MenuEntry menuEntry = new MenuEntry();

        Dish dish = new Dish(
                "Fish Dish",
                DishType.FISH
        );

        MenuEntryDish menuEntryDish = new MenuEntryDish(
                menuEntry,
                dish
        );

        reservation.setMenuEntryDish(menuEntryDish);

        assertEquals(menuEntryDish, reservation.getMenuEntryDish());
    }

    @Test
    void shouldSetAndGetReservationDateTime() {
        Reservation reservation = new Reservation();

        LocalDateTime reservationDateTime =
                LocalDateTime.of(2026, 5, 14, 12, 30);

        reservation.setReservationDateTime(reservationDateTime);

        assertEquals(reservationDateTime, reservation.getReservationDateTime());
    }

    @Test
    void shouldSetAndGetStatus() {
        Reservation reservation = new Reservation();

        reservation.setStatus(ReservationStatus.PENDING);

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }
}