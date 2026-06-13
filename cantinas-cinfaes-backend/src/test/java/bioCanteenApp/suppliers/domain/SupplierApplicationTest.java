package bioCanteenApp.suppliers.domain;

import bioCanteenApp.address.Address;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierApplicationTest {

    @Test
    void shouldCreateSupplierApplicationWithConstructor() {
        Address address = new Address("Rua Central", null, null, "Portugal", "4000-111");
        byte[] certificate = {1, 2, 3};

        SupplierCapacity capacity = new SupplierCapacity(
                "Rice",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                1000.0
        );

        LocalDate applicationDate = LocalDate.of(2026, 5, 14);

        SupplierApplication application = new SupplierApplication(
                "Supplier",
                "supplier@email.com",
                "912345678",
                address,
                certificate,
                123456789L,
                List.of(capacity),
                applicationDate
        );

        assertEquals("Supplier", application.getName());
        assertEquals("supplier@email.com", application.getEmail());
        assertEquals("912345678", application.getPhoneNumber());
        assertEquals(address, application.getAddress());
        assertArrayEquals(certificate, application.getBioCertificate());
        assertEquals(123456789L, application.getNif());
        assertEquals(List.of(capacity), application.getSupplierCapacity());
        assertEquals(applicationDate, application.getApplicationDate());
        assertEquals(SupplierApplicationStatus.PENDING, application.getStatus());
        assertEquals(InterviewStatus.TO_BE_DONE, application.getInterviewStatus());
    }

    @Test
    void shouldSetAndGetId() {
        SupplierApplication application = new SupplierApplication();

        application.setId(1L);

        assertEquals(1L, application.getId());
    }

    @Test
    void shouldSetAndGetName() {
        SupplierApplication application = new SupplierApplication();

        application.setName("New Supplier");

        assertEquals("New Supplier", application.getName());
    }

    @Test
    void shouldSetAndGetEmail() {
        SupplierApplication application = new SupplierApplication();

        application.setEmail("new@email.com");

        assertEquals("new@email.com", application.getEmail());
    }

    @Test
    void shouldSetAndGetAddress() {
        SupplierApplication application = new SupplierApplication();
        Address address = new Address("Rua Nova", null, null, "Portugal", "4000-222");

        application.setAddress(address);

        assertEquals(address, application.getAddress());
    }

    @Test
    void shouldSetAndGetPhoneNumber() {
        SupplierApplication application = new SupplierApplication();

        application.setPhoneNumber("923456789");

        assertEquals("923456789", application.getPhoneNumber());
    }

    @Test
    void shouldSetAndGetNif() {
        SupplierApplication application = new SupplierApplication();

        application.setNif(987654321L);

        assertEquals(987654321L, application.getNif());
    }

    @Test
    void shouldSetAndGetBioCertificate() {
        SupplierApplication application = new SupplierApplication();
        byte[] certificate = {4, 5, 6};

        application.setBioCertificate(certificate);

        assertArrayEquals(certificate, application.getBioCertificate());
    }

    @Test
    void shouldSetAndGetSupplierCapacity() {
        SupplierApplication application = new SupplierApplication();

        List<SupplierCapacity> capacities = new ArrayList<>();
        SupplierCapacity capacity = new SupplierCapacity(
                "Potato",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                500.0
        );
        capacities.add(capacity);

        application.setSupplierCapacity(capacities);

        assertEquals(capacities, application.getSupplierCapacity());
    }

    @Test
    void shouldSetAndGetApplicationDate() {
        SupplierApplication application = new SupplierApplication();
        LocalDate date = LocalDate.of(2026, 5, 14);

        application.setApplicationDate(date);

        assertEquals(date, application.getApplicationDate());
    }

    @Test
    void shouldSetAndGetStatus() {
        SupplierApplication application = new SupplierApplication();

        application.setStatus(SupplierApplicationStatus.PENDING);

        assertEquals(SupplierApplicationStatus.PENDING, application.getStatus());
    }

    @Test
    void shouldSetAndGetInterviewStatus() {
        SupplierApplication application = new SupplierApplication();

        application.setInterviewStatus(InterviewStatus.TO_BE_DONE);

        assertEquals(InterviewStatus.TO_BE_DONE, application.getInterviewStatus());
    }
}