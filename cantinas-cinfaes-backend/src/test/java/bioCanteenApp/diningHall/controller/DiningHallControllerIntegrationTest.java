package bioCanteenApp.diningHall.controller;

import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.service.IDiningHallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@AutoConfigureTestDatabase
class DiningHallControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDiningHallService diningHallService;

    private DiningHallDTO diningHallDTO;

    @BeforeEach
    void setUp() {
        diningHallDTO = DiningHallDTO.builder()
                .id(1L)
                .name("Refeitório A")
                .canteenId(10L)
                .canteenName("Cantina Central")
                .wastesCount(5)
                .build();
    }

    @Test
    void getAllDiningHalls_ShouldReturnListOfDiningHalls() throws Exception {
        List<DiningHallDTO> list = Arrays.asList(diningHallDTO);
        Mockito.when(diningHallService.getAllDiningHall()).thenReturn(list);

        mockMvc.perform(get("/api/dining-halls")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Refeitório A"))
                .andExpect(jsonPath("$[0].canteenId").value(10L))
                .andExpect(jsonPath("$[0].canteenName").value("Cantina Central"))
                .andExpect(jsonPath("$[0].wastesCount").value(5));
    }
}