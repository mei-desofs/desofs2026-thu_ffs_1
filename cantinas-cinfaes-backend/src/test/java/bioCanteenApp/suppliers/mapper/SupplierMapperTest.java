package bioCanteenApp.suppliers.mapper;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.dto.AddressDTO;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierMapperTest {

    private final SupplierMapper mapper = new SupplierMapper();

    private AddressDTO buildAddressDTO() {
        return AddressDTO.builder()
                .street("Rua Test")
                .municipality("CINFAES")
                .village("ANSIAES")
                .country("Portugal")
                .postalCode("4560-000")
                .build();
    }

    private Address buildAddress() {
        return new Address("Rua Test", Municipality.CINFAES, Village.ANSIAES, "Portugal", "4560-000");
    }

    private SupplierApplicationDTO buildApplicationDTO() {
        return SupplierApplicationDTO.builder()
                .name("Supplier Name")
                .email("supplier@test.com")
                .phoneNumber("912345678")
                .address(buildAddressDTO())
                .nif(123456789L)
                .applicationDate(LocalDate.of(2026, 1, 1))
                .supplierCapacity(List.of(
                        SupplierApplicationDTO.SupplierCapacityDTO.builder()
                                .productName("Product A")
                                .startDate(LocalDate.of(2026, 1, 1))
                                .endDate(LocalDate.of(2026, 12, 31))
                                .quantity(100.0)
                                .build()
                ))
                .build();
    }

    // ---- toDTO(Supplier) ----

    @Test
    void toDTO_supplier_withNull_returnsNull() {
        assertNull(mapper.toDTO((Supplier) null));
    }

    @Test
    void toDTO_supplier_mapsAllFields() {
        User user = new User("supplier@test.com", "Supplier Name", "pass");
        SupplierApplication app = new SupplierApplication(
                "Supplier Name", "supplier@test.com", "912345678",
                buildAddress(), null, 123456789L, List.of(), LocalDate.of(2026, 1, 1));
        app.setId(42L);

        Supplier supplier = new Supplier(user, "123456789", buildAddress(), "912345678", null, app);

        SupplierDTO dto = mapper.toDTO(supplier);

        assertNotNull(dto);
        assertEquals("Supplier Name", dto.getName());
        assertEquals("supplier@test.com", dto.getEmail());
        assertEquals("123456789", dto.getNif());
        assertEquals("912345678", dto.getPhoneNumber());
        assertEquals(42L, dto.getApplicationId());
        assertFalse(dto.isQuarantined());
        assertNull(dto.getCertifiedOrganic());
    }

    @Test
    void toDTO_supplier_withCertifiedOrganic_base64Encodes() {
        User user = new User("s@test.com", "S", "pass");
        SupplierApplication app = new SupplierApplication(
                "S", "s@test.com", "900000000",
                buildAddress(), null, 111111111L, List.of(), LocalDate.now());
        app.setId(1L);

        byte[] cert = "certificate-data".getBytes();
        Supplier supplier = new Supplier(user, "111111111", buildAddress(), "900000000", cert, app);

        SupplierDTO dto = mapper.toDTO(supplier);

        assertEquals(Base64.getEncoder().encodeToString(cert), dto.getCertifiedOrganic());
    }

    @Test
    void toDTO_supplier_withNullUser_mapsNullNameAndEmail() {
        SupplierApplication app = new SupplierApplication(
                "S", "s@test.com", "900000000",
                buildAddress(), null, 111111111L, List.of(), LocalDate.now());
        app.setId(1L);

        Supplier supplier = new Supplier(null, "111111111", buildAddress(), "900000000", null, app);

        SupplierDTO dto = mapper.toDTO(supplier);

        assertNull(dto.getName());
        assertNull(dto.getEmail());
    }

    // ---- toDTO(SupplierApplication) ----

    @Test
    void toDTO_supplierApplication_withNull_returnsNull() {
        assertNull(mapper.toDTO((SupplierApplication) null));
    }

    @Test
    void toDTO_supplierApplication_mapsAllFields() {
        SupplierApplication app = new SupplierApplication(
                "Supplier", "s@test.com", "912000000",
                buildAddress(), null, 987654321L,
                List.of(new SupplierCapacity("ProductA", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 50.0)),
                LocalDate.of(2026, 1, 1));

        SupplierApplicationDTO dto = mapper.toDTO(app);

        assertNotNull(dto);
        assertEquals("Supplier", dto.getName());
        assertEquals("s@test.com", dto.getEmail());
        assertEquals(987654321L, dto.getNif());
        assertEquals("PENDING", dto.getStatus());
        assertEquals("TO_BE_DONE", dto.getInterviewStatus());
        assertEquals(1, dto.getSupplierCapacity().size());
        assertEquals("ProductA", dto.getSupplierCapacity().get(0).getProductName());
        assertNull(dto.getBioCertificate());
    }

    @Test
    void toDTO_supplierApplication_withBioCertificate_base64Encodes() {
        byte[] cert = "bio-cert".getBytes();
        SupplierApplication app = new SupplierApplication(
                "S", "s@test.com", "900000000",
                buildAddress(), cert, 111111111L, List.of(), LocalDate.now());

        SupplierApplicationDTO dto = mapper.toDTO(app);

        assertEquals(Base64.getEncoder().encodeToString(cert), dto.getBioCertificate());
    }

    @Test
    void toDTO_supplierApplication_withNullInterviewStatus_mapsNull() {
        SupplierApplication app = new SupplierApplication(
                "S", "s@test.com", "900000000",
                buildAddress(), null, 111111111L, List.of(), LocalDate.now());
        app.setInterviewStatus(null);

        SupplierApplicationDTO dto = mapper.toDTO(app);

        assertNull(dto.getInterviewStatus());
    }

    // ---- toDomain(SupplierApplicationDTO) ----

    @Test
    void toDomain_supplierApplicationDTO_mapsFields() {
        SupplierApplicationDTO dto = buildApplicationDTO();

        SupplierApplication app = mapper.toDomain(dto);

        assertNotNull(app);
        assertEquals("Supplier Name", app.getName());
        assertEquals("supplier@test.com", app.getEmail());
        assertEquals(123456789L, app.getNif());
        assertEquals(SupplierApplicationStatus.PENDING, app.getStatus());
        assertEquals(InterviewStatus.TO_BE_DONE, app.getInterviewStatus());
        assertEquals(1, app.getSupplierCapacity().size());
        assertEquals("Product A", app.getSupplierCapacity().get(0).getProductName());
    }

    @Test
    void toDomain_supplierApplicationDTO_withBioCertificate_decodes() {
        byte[] cert = "bio-cert".getBytes();
        SupplierApplicationDTO dto = buildApplicationDTO();
        dto.setBioCertificate(Base64.getEncoder().encodeToString(cert));

        SupplierApplication app = mapper.toDomain(dto);

        assertArrayEquals(cert, app.getBioCertificate());
    }

    @Test
    void toDomain_supplierApplicationDTO_withNullCapacityAndDate_usesDefaults() {
        SupplierApplicationDTO dto = SupplierApplicationDTO.builder()
                .name("S")
                .email("s@test.com")
                .phoneNumber("900000000")
                .nif(111111111L)
                .supplierCapacity(null)
                .applicationDate(null)
                .build();

        SupplierApplication app = mapper.toDomain(dto);

        assertNotNull(app);
        assertTrue(app.getSupplierCapacity().isEmpty());
        assertNotNull(app.getApplicationDate());
    }

    // ---- toDomain(User, SupplierDTO, SupplierApplicationDTO) ----

    @Test
    void toDomain_supplierWithNullDto_returnsNull() {
        User user = new User("u@test.com", "U", "pass");
        assertNull(mapper.toDomain(user, null, buildApplicationDTO()));
    }

    @Test
    void toDomain_supplierWithNullApplicationDto_returnsNull() {
        User user = new User("u@test.com", "U", "pass");
        assertNull(mapper.toDomain(user, SupplierDTO.builder().build(), null));
    }

    @Test
    void toDomain_supplier_mapsFields() {
        User user = new User("s@test.com", "Supplier", "pass");
        SupplierDTO supplierDTO = SupplierDTO.builder()
                .nif("123456789")
                .address(buildAddressDTO())
                .phoneNumber("912345678")
                .build();

        Supplier supplier = mapper.toDomain(user, supplierDTO, buildApplicationDTO());

        assertNotNull(supplier);
        assertEquals(user, supplier.getUser());
        assertEquals("123456789", supplier.getNif());
        assertEquals("912345678", supplier.getPhoneNumber());
        assertNotNull(supplier.getAddress());
    }

    @Test
    void toDomain_supplier_withCertifiedOrganic_decodes() {
        User user = new User("s@test.com", "Supplier", "pass");
        byte[] cert = "cert-data".getBytes();
        SupplierDTO supplierDTO = SupplierDTO.builder()
                .nif("123456789")
                .certifiedOrganic(Base64.getEncoder().encodeToString(cert))
                .build();

        Supplier supplier = mapper.toDomain(user, supplierDTO, buildApplicationDTO());

        assertArrayEquals(cert, supplier.getCertifiedOrganic());
    }
}
