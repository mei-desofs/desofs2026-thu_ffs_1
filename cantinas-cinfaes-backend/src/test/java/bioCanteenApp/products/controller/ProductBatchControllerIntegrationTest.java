package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductBatchDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class ProductBatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllBatches() throws Exception {

        mockMvc.perform(get("/api/product-batches"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetBatchById() throws Exception {

        mockMvc.perform(get("/api/product-batches/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetBatchesByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetValidBatchesByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1/valid"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetValidStockByProduct() throws Exception {

        mockMvc.perform(get("/api/product-batches/product/1/stock"))
                .andExpect(status().isOk());
    }
}