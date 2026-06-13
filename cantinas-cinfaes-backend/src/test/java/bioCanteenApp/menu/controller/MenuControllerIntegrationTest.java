package bioCanteenApp.menu.controller;

import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.IMenuMapper;
import bioCanteenApp.menu.service.IMenuService;
import bioCanteenApp.provisioning.service.IProvisioningService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IMenuService menuService;

    @MockBean
    private DishService dishService;

    @MockBean
    private IProvisioningService provisioningService;

    @MockBean
    private IMenuMapper menuMapper;

    private MenuDto menuDto;

    @BeforeEach
    void setUp() {
        menuDto = new MenuDto();
        menuDto.setId(1L);
        menuDto.setWeekStartDate(LocalDate.of(2026, 6, 9));
        menuDto.setWeekEndDate(LocalDate.of(2026, 6, 15));
    }

    @Test
    @WithMockUser(roles = "DIETITIAN")
    void getAllMenus_AsDietitian_ShouldReturnOk() throws Exception {
        when(menuService.getAllMenus()).thenReturn(List.of(menuDto));

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMenus_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllMenus_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DIETITIAN")
    void createMenu_AsDietitian_ShouldReturnCreatedMenu() throws Exception {
        when(menuService.createMenu(any(MenuDto.class))).thenReturn(menuDto);

        mockMvc.perform(post("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menuDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "DIETITIAN")
    void getMenusByWeek_AsDietitian_ShouldReturnFilteredMenus() throws Exception {
        when(menuService.getAllMenus()).thenReturn(List.of(menuDto));

        mockMvc.perform(get("/api/menus/week")
                        .param("startDate", "2026-06-09")
                        .param("endDate", "2026-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "DIETITIAN")
    void getPlanningStats_AsDietitian_ShouldReturnStats() throws Exception {
        when(menuService.getPlanningStats()).thenReturn(Map.of("totalMenus", 5L));

        mockMvc.perform(get("/api/menus/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMenus").value(5));
    }
}
