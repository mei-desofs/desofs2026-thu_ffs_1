package bioCanteenApp.suppliers.controller;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void shouldApplyToSupplierPosition() throws Exception {
        SupplierApplicationDTO inputDto = new SupplierApplicationDTO();
        // Configure as propriedades necessárias do inputDto se houverem, ex:
        // inputDto.setCompanyName("BioSupplier");

        SupplierApplicationDTO savedDto = new SupplierApplicationDTO();

        when(supplierService.applyToSupplierPosition(any(SupplierApplicationDTO.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/suppliers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.approveSupplier(any(SupplierDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suppliers/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectSupplier() throws Exception {
        SupplierDTO dto = new SupplierDTO();

        when(supplierService.rejectSupplier(any(SupplierDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suppliers/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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