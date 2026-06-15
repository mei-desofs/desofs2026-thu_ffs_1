package bioCanteenApp.suppliers.controller;

import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.service.ISupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierControllerTest {

    private ISupplierService supplierService;
    private SupplierController controller;

    @BeforeEach
    void setUp() {
        supplierService = mock(ISupplierService.class);
        controller = new SupplierController(supplierService);
    }

    @Test
    void shouldApplyToSupplierPosition() {
        SupplierApplicationDTO dto = new SupplierApplicationDTO();
        MultipartFile certificate = mock(MultipartFile.class);

        when(supplierService.applyToSupplierPosition(dto, certificate))
                .thenReturn(dto);

        ResponseEntity<SupplierApplicationDTO> response =
                controller.applyToSupplierPosition(dto, certificate);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(supplierService).applyToSupplierPosition(dto, certificate);
    }

    @Test
    void shouldApproveSupplier() {
        SupplierDTO dto = new SupplierDTO();
        Long applicationId = 1L;

        when(supplierService.approveSupplier(applicationId))
                .thenReturn(dto);

        ResponseEntity<SupplierDTO> response =
                controller.approveSupplier(applicationId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(supplierService).approveSupplier(applicationId);
    }

    @Test
    void shouldRejectSupplier() {
        SupplierDTO dto = new SupplierDTO();
        Long applicationId = 1L;
        String reason = "Does not meet organic standards";

        when(supplierService.rejectSupplier(applicationId, reason))
                .thenReturn(dto);

        ResponseEntity<SupplierDTO> response =
                controller.rejectSupplier(applicationId, reason);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(supplierService).rejectSupplier(applicationId, reason);
    }

    @Test
    void shouldGetSupplierStats() {
        Map<String, Long> stats = Map.of(
                "approved", 10L,
                "pending", 5L
        );

        when(supplierService.getSupplierStats())
                .thenReturn(stats);

        ResponseEntity<Map<String, Long>> response =
                controller.getSupplierStats();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(stats, response.getBody());

        verify(supplierService).getSupplierStats();
    }

    @Test
    void shouldFindAllSuppliers() {
        List<SupplierDTO> suppliers = List.of(
                new SupplierDTO(),
                new SupplierDTO()
        );

        when(supplierService.findAllSuppliers())
                .thenReturn(suppliers);

        ResponseEntity<List<SupplierDTO>> response =
                controller.findAllSuppliers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(suppliers, response.getBody());

        verify(supplierService).findAllSuppliers();
    }

    @Test
    void shouldFindAllApplications() {
        List<SupplierApplicationDTO> applications = List.of(
                new SupplierApplicationDTO(),
                new SupplierApplicationDTO()
        );

        when(supplierService.findAllApplications())
                .thenReturn(applications);

        ResponseEntity<List<SupplierApplicationDTO>> response =
                controller.findAllApplications();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(applications, response.getBody());

        verify(supplierService).findAllApplications();
    }

    @Test
    void shouldFindAllSuppliersByOrderByProduct() {
        List<SupplierDTO> suppliers = List.of(
                new SupplierDTO(),
                new SupplierDTO()
        );

        when(supplierService.findAllSuppliersByOrderByProduct(1L))
                .thenReturn(suppliers);

        ResponseEntity<List<SupplierDTO>> response =
                controller.findAllSuppliersByOrderByProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(suppliers, response.getBody());

        verify(supplierService)
                .findAllSuppliersByOrderByProduct(1L);
    }

    @Test
    void shouldQuarantineSupplier() {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.quarantineSupplier(dto))
                .thenReturn(dto);

        ResponseEntity<SupplierDTO> response =
                controller.quarantineSupplier(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(supplierService).quarantineSupplier(dto);
    }

    @Test
    void shouldUnquarantineSupplier() {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.unquarantineSupplier(dto))
                .thenReturn(dto);

        ResponseEntity<SupplierDTO> response =
                controller.unquarantineSupplier(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(supplierService).unquarantineSupplier(dto);
    }

    @Test
    void shouldGetSuppliersByName() {
        List<SupplierDTO> suppliers = List.of(
                new SupplierDTO(),
                new SupplierDTO()
        );

        when(supplierService.getSuppliersByName("Supplier"))
                .thenReturn(suppliers);

        ResponseEntity<List<SupplierDTO>> response =
                controller.getSuppliersByName("Supplier");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(suppliers, response.getBody());

        verify(supplierService).getSuppliersByName("Supplier");
    }

    @Test
    void shouldGetSuppliersByVillage() {
        List<SupplierDTO> suppliers = List.of(
                new SupplierDTO(),
                new SupplierDTO()
        );

        when(supplierService.getSuppliersByVillage("ANSIAES"))
                .thenReturn(suppliers);

        ResponseEntity<List<SupplierDTO>> response =
                controller.getSuppliersByVillage("ANSIAES");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(suppliers, response.getBody());

        verify(supplierService)
                .getSuppliersByVillage("ANSIAES");
    }

    @Test
    void shouldGetSuppliersByMunicipality() {
        List<SupplierDTO> suppliers = List.of(
                new SupplierDTO(),
                new SupplierDTO()
        );

        when(supplierService.getSuppliersByMunicipality("RESENDE"))
                .thenReturn(suppliers);

        ResponseEntity<List<SupplierDTO>> response =
                controller.getSuppliersByMunicipality("RESENDE");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(suppliers, response.getBody());

        verify(supplierService)
                .getSuppliersByMunicipality("RESENDE");
    }

    @Test
    void shouldGetBioCertificate() {
        Long applicationId = 1L;
        byte[] fakePdfBytes = "PDF_CONTENT".getBytes();

        when(supplierService.getBioCertificate(applicationId))
                .thenReturn(fakePdfBytes);

        ResponseEntity<byte[]> response =
                controller.getBioCertificate(applicationId);

        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(fakePdfBytes, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains("bio_certificate_1.pdf"));

        verify(supplierService).getBioCertificate(applicationId);
    }
}