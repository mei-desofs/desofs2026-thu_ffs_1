package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class CanteenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllCanteens() throws Exception {

        mockMvc.perform(get("/api/canteens"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCanteenById() throws Exception {

        mockMvc.perform(get("/api/canteens/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldQuarantineVillage() throws Exception {

        mockMvc.perform(post("/api/canteens/quarantine/ANSIAES"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnquarantineVillage() throws Exception {

        mockMvc.perform(post("/api/canteens/unquarantine/ANSIAES"))
                .andExpect(status().isOk());
    }
}