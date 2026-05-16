package bioCanteenApp.suppliers.service;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.email.validator.EmailDomainValidator;
import bioCanteenApp.products.domain.*;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.mapper.ISupplierMapper;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private ISupplierMapper supplierMapper;
    private ISupplierRepo supplierRepo;
    private IProductRepo productRepo;
    private IUserRepo userRepo;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private EmailDomainValidator emailDomainValidator;

    private SupplierService service;

    @BeforeEach
    void setUp() {
        supplierMapper = mock(ISupplierMapper.class);
        supplierRepo = mock(ISupplierRepo.class);
        productRepo = mock(IProductRepo.class);
        userRepo = mock(IUserRepo.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailDomainValidator = mock(EmailDomainValidator.class);

        service = new SupplierService(
                supplierMapper,
                supplierRepo,
                productRepo,
                userRepo,
                emailService,
                passwordEncoder,
                emailDomainValidator
        );
    }

    @Test
    void shouldApplyToSupplierPosition() {
        SupplierApplicationDTO dto = mock(SupplierApplicationDTO.class);
        SupplierApplicationDTO resultDto = mock(SupplierApplicationDTO.class);

        SupplierCapacity capacity = new SupplierCapacity(
                "Rice",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                1000.0
        );

        SupplierApplication application = createSupplierApplication();

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierMapper.toDomain(dto)).thenReturn(application);
        when(supplierMapper.toDTO(application)).thenReturn(resultDto);

        SupplierApplicationDTO result = service.applyToSupplierPosition(dto);

        assertEquals(resultDto, result);
        verify(emailDomainValidator).validate("supplier@email.com");
        verify(supplierRepo).save(application);
        verify(supplierMapper).toDTO(application);
    }

    @Test
    void shouldApproveSupplier() {
        SupplierDTO dto = mock(SupplierDTO.class);
        SupplierDTO resultDto = mock(SupplierDTO.class);

        SupplierApplication application = createSupplierApplication();
        Supplier supplier = createSupplier("supplier@email.com");

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierRepo.findByEmail("supplier@email.com"))
                .thenReturn(Optional.of(application));
        when(userRepo.findByEmail("supplier@email.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(supplierRepo.findAll())
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(resultDto);

        SupplierDTO result = service.approveSupplier(dto);

        assertEquals(resultDto, result);
        assertEquals(SupplierApplicationStatus.APPROVED, application.getStatus());

        verify(supplierRepo).save(application);
        verify(userRepo).save(any(User.class));
        verify(emailService).sendSupplierWelcomeEmail(eq("supplier@email.com"), anyString());
    }

    @Test
    void shouldThrowWhenApplicationNotFoundOnApproveSupplier() {
        SupplierDTO dto = mock(SupplierDTO.class);

        when(dto.getEmail()).thenReturn("missing@email.com");
        when(supplierRepo.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.approveSupplier(dto)
        );

        verify(emailService, never()).sendSupplierWelcomeEmail(any(), any());
    }

    @Test
    void shouldRejectSupplier() {
        SupplierDTO dto = mock(SupplierDTO.class);
        SupplierDTO resultDto = mock(SupplierDTO.class);

        SupplierApplication application = createSupplierApplication();
        Supplier supplier = createSupplier("supplier@email.com");

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierRepo.findByEmail("supplier@email.com"))
                .thenReturn(Optional.of(application));
        when(supplierRepo.findAll())
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(resultDto);

        SupplierDTO result = service.rejectSupplier(dto);

        assertEquals(resultDto, result);
        assertEquals(SupplierApplicationStatus.REJECTED, application.getStatus());

        verify(supplierRepo).save(application);
    }

    @Test
    void shouldGetSupplierStats() {
        when(supplierRepo.countTotalSuppliers()).thenReturn(10L);
        when(supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.APPROVED)).thenReturn(4L);
        when(supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.PENDING)).thenReturn(5L);
        when(supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.REJECTED)).thenReturn(1L);

        Map<String, Long> result = service.getSupplierStats();

        assertEquals(10L, result.get("totalSuppliers"));
        assertEquals(4L, result.get("approvedSuppliers"));
        assertEquals(5L, result.get("pendingSuppliers"));
        assertEquals(1L, result.get("rejectedSuppliers"));
    }

    @Test
    void shouldFindAllSuppliers() {
        Supplier supplier1 = createSupplier("supplier1@email.com");
        Supplier supplier2 = createSupplier("supplier2@email.com");

        SupplierDTO dto1 = mock(SupplierDTO.class);
        SupplierDTO dto2 = mock(SupplierDTO.class);

        when(supplierRepo.findAll()).thenReturn(List.of(supplier1, supplier2));
        when(supplierMapper.toDTO(supplier1)).thenReturn(dto1);
        when(supplierMapper.toDTO(supplier2)).thenReturn(dto2);

        List<SupplierDTO> result = service.findAllSuppliers();

        assertEquals(List.of(dto1, dto2), result);
    }

    @Test
    void shouldFindAllSuppliersByOrderByProduct() {
        Product product = createProduct();
        Supplier supplier = createSupplier("supplier@email.com");
        SupplierDTO dto = mock(SupplierDTO.class);

        when(productRepo.findById(1L)).thenReturn(product);
        when(supplierRepo.findAllSuppliersByOrderByProduct(product))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier)).thenReturn(dto);

        List<SupplierDTO> result = service.findAllSuppliersByOrderByProduct(1L);

        assertEquals(List.of(dto), result);
    }

    @Test
    void shouldFindAllApplications() {
        SupplierApplication application = createSupplierApplication();
        SupplierApplicationDTO dto = mock(SupplierApplicationDTO.class);

        when(supplierRepo.findAllApplications())
                .thenReturn(List.of(application));
        when(supplierMapper.toDTO(application))
                .thenReturn(dto);

        List<SupplierApplicationDTO> result = service.findAllApplications();

        assertEquals(List.of(dto), result);
    }

    @Test
    void shouldQuarantineSupplier() {
        SupplierDTO dto = mock(SupplierDTO.class);
        SupplierDTO resultDto = mock(SupplierDTO.class);

        Supplier supplier = createSupplier("supplier@email.com");
        ProductBatch batch = createProductBatch(supplier);

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierRepo.findBySupplierEmail("supplier@email.com"))
                .thenReturn(supplier);
        when(productRepo.findProductsBySupplier(supplier))
                .thenReturn(List.of(batch));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(resultDto);

        SupplierDTO result = service.quarantineSupplier(dto);

        assertEquals(resultDto, result);
        assertTrue(supplier.isQuarantined());
        assertTrue(batch.isQuarantined());

        verify(supplierRepo).save(supplier);
        verify(productRepo).save(batch);
    }

    @Test
    void shouldUnquarantineSupplier() {
        SupplierDTO dto = mock(SupplierDTO.class);
        SupplierDTO resultDto = mock(SupplierDTO.class);

        Supplier supplier = createSupplier("supplier@email.com");
        supplier.setQuarantined(true);

        ProductBatch batch = createProductBatch(supplier);
        batch.setQuarantined(true);

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierRepo.findBySupplierEmail("supplier@email.com"))
                .thenReturn(supplier);
        when(productRepo.findProductsBySupplier(supplier))
                .thenReturn(List.of(batch));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(resultDto);

        SupplierDTO result = service.unquarantineSupplier(dto);

        assertEquals(resultDto, result);
        assertFalse(supplier.isQuarantined());
        assertFalse(batch.isQuarantined());

        verify(supplierRepo).save(supplier);
        verify(productRepo).save(batch);
    }

    @Test
    void shouldGetSuppliersByName() {
        Supplier supplier = createSupplier("supplier@email.com");
        SupplierDTO dto = mock(SupplierDTO.class);

        when(supplierRepo.findSuppliersByName("Supplier"))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(dto);

        List<SupplierDTO> result = service.getSuppliersByName("Supplier");

        assertEquals(List.of(dto), result);
    }

    @Test
    void shouldGetSuppliersByVillage() {
        Supplier supplier = createSupplier("supplier@email.com");
        SupplierDTO dto = mock(SupplierDTO.class);

        when(supplierRepo.findSuppliersByVillage("ANSIAES"))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(dto);

        List<SupplierDTO> result = service.getSuppliersByVillage("ANSIAES");

        assertEquals(List.of(dto), result);
    }

    @Test
    void shouldGetSuppliersByMunicipality() {
        Supplier supplier = createSupplier("supplier@email.com");
        SupplierDTO dto = mock(SupplierDTO.class);

        when(supplierRepo.findSuppliersByMunicipality("RESENDE"))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDTO(supplier))
                .thenReturn(dto);

        List<SupplierDTO> result = service.getSuppliersByMunicipality("RESENDE");

        assertEquals(List.of(dto), result);
    }

    private Supplier createSupplier(String email) {
        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.ANSIAES,
                "Portugal",
                "4000-111"
        );

        User user = new User(
                email,
                "Supplier",
                "password",
                Role.USER
        );

        SupplierApplication application = createSupplierApplication();

        return new Supplier(
                user,
                "123456789",
                address,
                "912345678",
                new byte[]{1, 2, 3},
                application
        );
    }

    private SupplierApplication createSupplierApplication() {
        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.ANSIAES,
                "Portugal",
                "4000-111"
        );

        SupplierCapacity capacity = new SupplierCapacity(
                "Rice",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                1000.0
        );

        return new SupplierApplication(
                "Supplier",
                "supplier@email.com",
                "912345678",
                address,
                new byte[]{1, 2, 3},
                123456789L,
                List.of(capacity),
                LocalDate.of(2026, 5, 14)
        );
    }

    private Product createProduct() {
        return new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );
    }

    private ProductBatch createProductBatch(Supplier supplier) {
        return new ProductBatch(
                createProduct(),
                10.0,
                LocalDate.of(2026, 5, 14),
                true,
                supplier
        );
    }
}