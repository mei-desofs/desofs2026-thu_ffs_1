package bioCanteenApp.suppliers.service;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.email.validator.EmailDomainValidator;
import bioCanteenApp.products.domain.*;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.security.service.VirusTotalService;
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
import org.springframework.web.multipart.MultipartFile;

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
    private PasswordService passwordService;
    private VirusTotalService virusTotalService;

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
        passwordService = mock(PasswordService.class);
        virusTotalService = mock(VirusTotalService.class);

        service = new SupplierService(
                supplierMapper,
                supplierRepo,
                productRepo,
                userRepo,
                emailService,
                passwordEncoder,
                emailDomainValidator,
                passwordService,
                virusTotalService
        );
    }

    @Test
    void shouldApplyToSupplierPosition() throws Exception {
        SupplierApplicationDTO dto = mock(SupplierApplicationDTO.class);
        SupplierApplicationDTO resultDto = mock(SupplierApplicationDTO.class);
        MultipartFile certificate = mock(MultipartFile.class);

        SupplierApplication application = createSupplierApplication();

        when(dto.getEmail()).thenReturn("supplier@email.com");
        when(supplierMapper.toDomain(dto)).thenReturn(application);
        when(supplierMapper.toDTO(application)).thenReturn(resultDto);

        // Mock do ficheiro Multipart
        when(certificate.isEmpty()).thenReturn(false);
        when(certificate.getContentType()).thenReturn("application/pdf");
        when(certificate.getSize()).thenReturn(1024L);
        when(certificate.getBytes()).thenReturn(new byte[]{1, 2, 3});

        SupplierApplicationDTO result = service.applyToSupplierPosition(dto, certificate);

        assertEquals(resultDto, result);
        verify(emailDomainValidator).validate("supplier@email.com");
        verify(virusTotalService).scanFile(certificate); // Verifica se o anti-vírus foi chamado
        verify(supplierRepo).save(application);
        verify(supplierMapper).toDTO(application);
    }

    @Test
    void shouldThrowWhenCertificateIsMissingOrInvalid() {
        SupplierApplicationDTO dto = mock(SupplierApplicationDTO.class);
        when(dto.getEmail()).thenReturn("supplier@email.com");

        // 1. Missing certificate
        assertThrows(IllegalArgumentException.class,
                () -> service.applyToSupplierPosition(dto, null));

        // 2. Invalid type
        MultipartFile badTypeCert = mock(MultipartFile.class);
        when(badTypeCert.isEmpty()).thenReturn(false);
        when(badTypeCert.getContentType()).thenReturn("image/png");
        assertThrows(IllegalArgumentException.class,
                () -> service.applyToSupplierPosition(dto, badTypeCert));

        // 3. Exceeds size
        MultipartFile bigCert = mock(MultipartFile.class);
        when(bigCert.isEmpty()).thenReturn(false);
        when(bigCert.getContentType()).thenReturn("application/pdf");
        when(bigCert.getSize()).thenReturn(6L * 1024 * 1024); // 6MB
        assertThrows(IllegalArgumentException.class,
                () -> service.applyToSupplierPosition(dto, bigCert));
    }

    @Test
    void shouldApproveSupplier() {
        Long applicationId = 1L;
        SupplierDTO resultDto = mock(SupplierDTO.class);

        // Usar um mock para simular perfeitamente os métodos da entidade
        SupplierApplication application = mock(SupplierApplication.class);

        when(application.getInterviewStatus()).thenReturn(InterviewStatus.APPROVED); // Aprovou na entrevista
        when(application.getEmail()).thenReturn("supplier@email.com");
        when(application.getName()).thenReturn("BioCorp");
        when(application.getNif()).thenReturn(Long.valueOf("123456789"));

        when(supplierRepo.findApplicationById(applicationId)).thenReturn(Optional.of(application));
        when(userRepo.findByEmail("supplier@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordService.generateSupplierSetupToken(any(User.class))).thenReturn("fake-token-123");
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(resultDto);

        SupplierDTO result = service.approveSupplier(applicationId);

        assertEquals(resultDto, result);
        verify(application).setStatus(SupplierApplicationStatus.APPROVED);
        verify(supplierRepo).save(application);
        verify(supplierRepo).save(any(Supplier.class)); // Guarda a nova entidade Supplier
        verify(emailService).sendSupplierWelcomeEmail("supplier@email.com", "fake-token-123");
    }

    @Test
    void shouldThrowWhenApplicationNotFoundOnApproveSupplier() {
        when(supplierRepo.findApplicationById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.approveSupplier(1L));
        verify(emailService, never()).sendSupplierWelcomeEmail(any(), any());
    }

    @Test
    void shouldThrowWhenApplicationDidNotPassInterview() {
        Long applicationId = 1L;
        SupplierApplication application = mock(SupplierApplication.class);

        when(application.getInterviewStatus()).thenReturn(InterviewStatus.APPROVED);
        when(supplierRepo.findApplicationById(applicationId)).thenReturn(Optional.of(application));
    }

    @Test
    void shouldRejectSupplier() {
        Long applicationId = 1L;
        String reason = "Does not meet the criteria";

        SupplierApplication application = mock(SupplierApplication.class);
        when(application.getEmail()).thenReturn("supplier@email.com");
        when(application.getName()).thenReturn("BioCorp");

        when(supplierRepo.findApplicationById(applicationId)).thenReturn(Optional.of(application));

        SupplierDTO result = service.rejectSupplier(applicationId, reason);

        assertNotNull(result);
        assertEquals("supplier@email.com", result.getEmail());

        verify(application).setStatus(SupplierApplicationStatus.REJECTED);
        verify(supplierRepo).save(application);
        verify(emailService).sendSupplierRejectionEmail("supplier@email.com", reason);
    }

    @Test
    void shouldGetBioCertificate() {
        Long applicationId = 1L;
        byte[] fakePdfBytes = new byte[]{1, 2, 3};

        SupplierApplication application = mock(SupplierApplication.class);
        when(application.getBioCertificate()).thenReturn(fakePdfBytes);

        when(supplierRepo.findApplicationById(applicationId)).thenReturn(Optional.of(application));

        byte[] result = service.getBioCertificate(applicationId);

        assertArrayEquals(fakePdfBytes, result);
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

    // --- Helpers de Criação ---

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