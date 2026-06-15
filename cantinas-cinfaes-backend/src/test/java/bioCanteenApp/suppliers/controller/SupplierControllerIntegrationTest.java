package bioCanteenApp.suppliers.controller;

import bioCanteenApp.security.service.VirusTotalService;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.service.ISupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
@AutoConfigureTestDatabase
class SupplierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ISupplierService supplierService;

    @MockBean
    private VirusTotalService virusTotalService;

    @Test
    void shouldApplyToSupplierPosition() throws Exception {
        SupplierApplicationDTO inputDto = new SupplierApplicationDTO();
        SupplierApplicationDTO savedDto = new SupplierApplicationDTO();

        when(supplierService.applyToSupplierPosition(any(SupplierApplicationDTO.class), any()))
                .thenReturn(savedDto);

        MockMultipartFile certificate = new MockMultipartFile(
                "certificate", "certificado.pdf", MediaType.APPLICATION_PDF_VALUE, "fake-pdf-content".getBytes()
        );

        // Altera o nome de "dto" para "application" para coincidir com o @RequestPart("application")
        MockMultipartFile dtoPart = new MockMultipartFile(
                "application",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(inputDto)
        );

        mockMvc.perform(multipart("/api/suppliers/apply")
                        .file(certificate)
                        .file(dtoPart))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        // O serviço recebe um Long (applicationId) e devolve o DTO
        when(supplierService.approveSupplier(anyLong())).thenReturn(dto);

        // O Endpoint real é um POST com o ID no URL
        mockMvc.perform(post("/api/suppliers/approve/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();
        when(supplierService.rejectSupplier(anyLong(), anyString())).thenReturn(dto);

        // Envia o motivo no .content() e usa text/plain
        mockMvc.perform(post("/api/suppliers/reject/{id}", 2L)
                        .content("Doesn't meet bio standards")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetSupplierStats() throws Exception {
        Map<String, Long> mockStats = Map.of("active", 10L, "pending", 2L);

        when(supplierService.getSupplierStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/suppliers/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(10))
                .andExpect(jsonPath("$.pending").value(2));
    }

    @Test
    void shouldFindAllSuppliers() throws Exception {
        SupplierDTO s1 = new SupplierDTO();
        SupplierDTO s2 = new SupplierDTO();

        when(supplierService.findAllSuppliers()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFindAllApplications() throws Exception {
        SupplierApplicationDTO app1 = new SupplierApplicationDTO();

        when(supplierService.findAllApplications()).thenReturn(List.of(app1));

        mockMvc.perform(get("/api/suppliers/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFindAllSuppliersByOrderByProduct() throws Exception {
        SupplierDTO s1 = new SupplierDTO();

        when(supplierService.findAllSuppliersByOrderByProduct(1L)).thenReturn(List.of(s1));

        mockMvc.perform(get("/api/suppliers/order/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldQuarantineSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.quarantineSupplier(any(SupplierDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suppliers/quarantine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnquarantineSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.unquarantineSupplier(any(SupplierDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suppliers/unquarantine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetSuppliersByName() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.getSuppliersByName("BioCorp")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/suppliers/filter/name/{name}", "BioCorp"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetSuppliersByVillage() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.getSuppliersByVillage("Anciaes")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/suppliers/filter/village/{village}", "Anciaes"))
                .andExpect(status().isOk());
    }
}