package bioCanteenApp.waste.controller;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.repository.CanteenRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.service.IWasteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@AutoConfigureTestDatabase
class WasteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepository;

    @MockBean
    private IWasteService wasteService;

    private WasteDTO mockWasteDTO;

    @Autowired
    private CanteenRepo canteenRepository;

    @BeforeEach
    void setUp() {

        mockWasteDTO = WasteDTO.builder()
                .totalMealsReserved(150.0)
                .notServedWaste(12.5)
                .servedWaste(8.3)
                .totalMealsConsumed(137.5)
                .build();
    }

    @Test
    void getDailyWaste_ShouldReturnStatusOkAndCorrectJsonStructure() throws Exception {
        when(wasteService.getDailyWaste()).thenReturn(mockWasteDTO);

        mockMvc.perform(get("/api/waste/daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.totalMealsReserved").value(150.0))
                .andExpect(jsonPath("$.notServedWaste").value(12.5))
                .andExpect(jsonPath("$.servedWaste").value(8.3))
                .andExpect(jsonPath("$.totalMealsConsumed").value(137.5));
    }

    @Test
    void getKPIs_ForCanteenManager_ShouldReturnPopulatedDTO() throws Exception {
        Canteen canteen = new Canteen(
                "Cantina Central",
                new Address("Rua Principal",
                        Municipality.RESENDE,
                        Village.ANSIAES,
                        "Porto",
                        "4150-123"),
                150,
                false
        );

        Canteen savedCanteen = canteenRepository.save(canteen);

        User manager = new User(
                "email@test.com",
                "Manager Name",
                "password123",
                Role.CANTEEN_MANAGER
        );

        manager.setCanteen(canteen);

        User savedUser = userRepository.save(manager);

        LocalDate[] mockRange = new LocalDate[]{LocalDate.now().minusDays(30), LocalDate.now()};
        when(wasteService.getDateRange("month")).thenReturn(mockRange);

        when(wasteService.aggregateWaste(
                eq(savedUser.getCanteen().getId()),
                eq(null),
                eq(null),
                any(LocalDate.class),
                any(LocalDate.class))
        ).thenReturn(mockWasteDTO);

        mockMvc.perform(get("/api/waste/kpis/month")
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMealsReserved").value(150.0))
                .andExpect(jsonPath("$.notServedWaste").value(12.5));
    }

    @Test
    void getKPIs_WhenUserDoesNotExist_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/waste/kpis/month")
                        .param("userId", "999"))
                .andExpect(status().isBadRequest());
    }
}