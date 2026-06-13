package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductBatchDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles = "ADMIN")
class ProductBatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllBatches() throws Exception {

        mockMvc.perform(get("/api/product-batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetBatchById() throws Exception {

        mockMvc.perform(get("/api/product-batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldGetBatchesByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetValidBatchesByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1/valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetValidStockByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber()); // Assumption based on stock typically being a number
    }
}