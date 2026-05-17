package bioCanteenApp.menu.domain;

import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    @Test
    void shouldCreateEmptyMenu() {
        Menu menu = new Menu();

        assertNull(menu.getId());
        assertNull(menu.getWeekStartDate());
        assertNull(menu.getWeekEndDate());
        assertEquals(MenuStatus.GENERATED, menu.getStatus());
        assertNotNull(menu.getEntries());
        assertTrue(menu.getEntries().isEmpty());
        assertNull(menu.getDietician());
    }

    @Test
    void shouldCreateMenuWithFullConstructor() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 7);

        List<MenuEntry> entries = new ArrayList<>();

        User dietician = new User(
                "dietician@email.com",
                "Dietician",
                "password",
                Role.USER
        );

        Menu menu = new Menu(
                startDate,
                endDate,
                MenuStatus.PUBLISHED,
                entries,
                dietician
        );

        assertEquals(startDate, menu.getWeekStartDate());
        assertEquals(endDate, menu.getWeekEndDate());
        assertEquals(MenuStatus.PUBLISHED, menu.getStatus());
        assertEquals(entries, menu.getEntries());
        assertEquals(dietician, menu.getDietician());
    }

    @Test
    void shouldCreateMenuWithSimplifiedConstructor() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 7);

        Menu menu = new Menu(
                startDate,
                endDate,
                MenuStatus.GENERATED
        );

        assertEquals(startDate, menu.getWeekStartDate());
        assertEquals(endDate, menu.getWeekEndDate());
        assertEquals(MenuStatus.GENERATED, menu.getStatus());

        assertNotNull(menu.getEntries());
        assertTrue(menu.getEntries().isEmpty());

        assertNull(menu.getDietician());
    }

    @Test
    void shouldSetAndGetId() {
        Menu menu = new Menu();

        menu.setId(1L);

        assertEquals(1L, menu.getId());
    }

    @Test
    void shouldSetAndGetWeekStartDate() {
        Menu menu = new Menu();

        LocalDate startDate = LocalDate.of(2026, 5, 1);

        menu.setWeekStartDate(startDate);

        assertEquals(startDate, menu.getWeekStartDate());
    }

    @Test
    void shouldSetAndGetWeekEndDate() {
        Menu menu = new Menu();

        LocalDate endDate = LocalDate.of(2026, 5, 7);

        menu.setWeekEndDate(endDate);

        assertEquals(endDate, menu.getWeekEndDate());
    }

    @Test
    void shouldSetAndGetStatus() {
        Menu menu = new Menu();

        menu.setStatus(MenuStatus.PUBLISHED);

        assertEquals(MenuStatus.PUBLISHED, menu.getStatus());
    }

    @Test
    void shouldSetAndGetEntries() {
        Menu menu = new Menu();

        List<MenuEntry> entries = new ArrayList<>();

        menu.setEntries(entries);

        assertEquals(entries, menu.getEntries());
    }

    @Test
    void shouldSetAndGetDietician() {
        Menu menu = new Menu();

        User dietician = new User(
                "dietician@email.com",
                "Dietician",
                "password",
                Role.CANTEEN_MANAGER
        );

        menu.setDietician(dietician);

        assertEquals(dietician, menu.getDietician());
    }
}