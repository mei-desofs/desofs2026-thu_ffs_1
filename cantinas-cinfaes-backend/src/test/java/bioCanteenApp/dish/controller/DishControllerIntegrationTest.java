package bioCanteenApp.dish.controller;

import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class DishControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetDishTypes() throws Exception {

        mockMvc.perform(get("/api/dishes/dish-types"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGenerateDishInformation() throws Exception {

        GetDishDTO dto = new GetDishDTO();

        dto.setIngredients(new ArrayList<>());

        mockMvc.perform(
                        post("/api/dishes/nutrition-allergens")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAlternatives() throws Exception {

        mockMvc.perform(get("/api/dishes/alternatives/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReplaceDish() throws Exception {

        mockMvc.perform(
                        put("/api/dishes/1/replace")
                                .param("newDishId", "2")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetOrganicProducts() throws Exception {

        mockMvc.perform(get("/api/dishes/organic"))
                .andExpect(status().isOk());
    }
}