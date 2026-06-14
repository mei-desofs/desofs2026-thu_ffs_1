package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.suppliers.dto.AddressDTO;
import bioCanteenApp.canteens.service.ICanteenService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@AutoConfigureTestDatabase
class CanteenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICanteenService canteenService;

    private CanteenDTO canteenDTO;
    private AddressDTO addressDTO;

    @BeforeEach
    void setUp() {
        addressDTO = new AddressDTO();
        addressDTO.setVillage("Vila Nova");
        addressDTO.setMunicipality("Porto");

        canteenDTO = CanteenDTO.builder()
                .name("Cantina Central")
                .location(addressDTO)
                .capacity(150)
                .isQuarantine(false)
                .canCookDishes(true)
                .build();
    }

    @Test
    void createCanteen_ShouldReturnCreatedCanteen() throws Exception {
        Mockito.when(canteenService.createCanteen(any(CanteenDTO.class))).thenReturn(canteenDTO);

        mockMvc.perform(post("/api/canteens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(canteenDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cantina Central"))
                .andExpect(jsonPath("$.capacity").value(150))
                .andExpect(jsonPath("$.isQuarantine").value(false))
                .andExpect(jsonPath("$.location.village").value("Vila Nova"));
    }

    @Test
    void getAllCanteens_ShouldReturnListOfCanteens() throws Exception {
        List<CanteenDTO> list = Arrays.asList(canteenDTO);
        Mockito.when(canteenService.getAllCanteens()).thenReturn(list);

        mockMvc.perform(get("/api/canteens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cantina Central"))
                .andExpect(jsonPath("$[0].location.municipality").value("Porto"));
    }

    @Test
    void getCanteenById_ShouldReturnCanteen() throws Exception {
        // Como o teu DTO atual não tem ID, assumimos que o serviço ainda recebe o ID por parâmetro no URL
        Mockito.when(canteenService.getById(1L)).thenReturn(canteenDTO);

        mockMvc.perform(get("/api/canteens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cantina Central"))
                .andExpect(jsonPath("$.canCookDishes").value(true));
    }

    @Test
    void quarantineVillage_ShouldReturnQuarantinedCanteens() throws Exception {
        canteenDTO.setIsQuarantine(true);
        List<CanteenDTO> list = Arrays.asList(canteenDTO);
        Mockito.when(canteenService.quarantineCanteensByVillage("Vila Nova")).thenReturn(list);

        mockMvc.perform(post("/api/canteens/quarantine/Vila Nova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location.village").value("Vila Nova"))
                .andExpect(jsonPath("$[0].isQuarantine").value(true));
    }

    @Test
    void getCanteensByMunicipality_ShouldReturnFilteredCanteens() throws Exception {
        List<CanteenDTO> list = Arrays.asList(canteenDTO);
        Mockito.when(canteenService.getByMunicipality("Porto")).thenReturn(list);

        mockMvc.perform(get("/api/canteens/filter/municipality/Porto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location.municipality").value("Porto"));
    }

    @Test
    void getCanteensByVillage_ShouldReturnFilteredCanteens() throws Exception {
        List<CanteenDTO> list = Arrays.asList(canteenDTO);
        Mockito.when(canteenService.getByVillage("Vila Nova")).thenReturn(list);

        mockMvc.perform(get("/api/canteens/filter/village/Vila Nova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location.village").value("Vila Nova"));
    }
}