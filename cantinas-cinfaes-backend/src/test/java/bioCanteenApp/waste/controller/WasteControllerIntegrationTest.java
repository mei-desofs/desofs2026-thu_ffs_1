package bioCanteenApp.waste.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class WasteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetDailyWaste() throws Exception {

        mockMvc.perform(get("/api/waste/daily"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetWeeklyWaste() throws Exception {

        mockMvc.perform(get("/api/waste/weekly"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMonthlyWaste() throws Exception {

        mockMvc.perform(get("/api/waste/monthly"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllWaste() throws Exception {

        mockMvc.perform(get("/api/waste/all"))
                .andExpect(status().isOk());
    }
}