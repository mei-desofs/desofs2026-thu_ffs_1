package bioCanteenApp.users.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithDefaultRole() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        assertEquals("user@email.com", user.getEmail());
        assertEquals("John Doe", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getCanteen());
        assertNull(user.getDiningHall());
    }

    @Test
    void shouldCreateUserWithRole() {
        User user = new User(
                "admin@email.com",
                "Admin",
                "password",
                Role.ADMIN
        );

        assertEquals("admin@email.com", user.getEmail());
        assertEquals("Admin", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldCreateUserAssociatedWithCanteen() {
        Address address = new Address(
                "Rua Central",
                Municipality.CINFAES,
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

        User user = new User(
                "manager@email.com",
                "Manager",
                "password",
                Role.USER,
                canteen
        );

        assertEquals("manager@email.com", user.getEmail());
        assertEquals("Manager", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals(canteen, user.getCanteen());
        assertNull(user.getDiningHall());
    }

    @Test
    void shouldCreateUserAssociatedWithDiningHall() {
        Address address = new Address(
                "Rua Central",
                Municipality.CELORICO_DE_BASTO,
                Village.GESTAÇO,
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
                "employee@email.com",
                "Employee",
                "password",
                Role.USER,
                diningHall
        );

        assertEquals("employee@email.com", user.getEmail());
        assertEquals("Employee", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getCanteen());
        assertEquals(diningHall, user.getDiningHall());
    }

    @Test
    void shouldCreateUserAssociatedWithCanteenAndDiningHall() {
        Address address = new Address(
                "Rua Central",
                Municipality.CINFAES,
                Village.CANDEMIL,
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
                "employee@email.com",
                "Employee",
                "password",
                Role.USER,
                canteen,
                diningHall
        );

        assertEquals("employee@email.com", user.getEmail());
        assertEquals("Employee", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals(canteen, user.getCanteen());
        assertEquals(diningHall, user.getDiningHall());
    }

    @Test
    void shouldSetAndGetId() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        user.setId(1L);

        assertEquals(1L, user.getId());
    }

    @Test
    void shouldSetAndGetEmail() {
        User user = new User(
                "old@email.com",
                "John Doe",
                "password"
        );

        user.setEmail("new@email.com");

        assertEquals("new@email.com", user.getEmail());
    }

    @Test
    void shouldSetAndGetName() {
        User user = new User(
                "user@email.com",
                "Old Name",
                "password"
        );

        user.setName("New Name");

        assertEquals("New Name", user.getName());
    }

    @Test
    void shouldSetAndGetPassword() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "oldPassword"
        );

        user.setPassword("newPassword");

        assertEquals("newPassword", user.getPassword());
    }

    @Test
    void shouldSetAndGetRole() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        user.setRole(Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldSetAndGetCanteen() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.LOIVOS_DO_MONTE,
                "Portugal",
                "4000-111"
        );

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                address,
                300,
                true
        );

        user.setCanteen(canteen);

        assertEquals(canteen, user.getCanteen());
    }

    @Test
    void shouldSetAndGetDiningHall() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        Address address = new Address(
                "Rua Central",
                Municipality.MARCO_DE_CANAVESES,
                Village.GONDAR,
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

        user.setDiningHall(diningHall);

        assertEquals(diningHall, user.getDiningHall());
    }

    @Test
    void shouldSetAndGetPasswordChangedAt() {
        User user = new User(
                "user@email.com",
                "John Doe",
                "password"
        );

        LocalDateTime passwordChangedAt =
                LocalDateTime.of(2026, 5, 14, 10, 30);

        user.setPasswordChangedAt(passwordChangedAt);

        assertEquals(passwordChangedAt, user.getPasswordChangedAt());
    }
}