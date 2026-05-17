package bioCanteenApp.dish.controller;

import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.service.IDishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishControllerTest {

    private IDishService dishService;
    private DishController controller;

    @BeforeEach
    void setUp() {
        dishService = mock(IDishService.class);

        controller = new DishController(dishService);
    }

    @Test
    void shouldCreateDish() {
        DishDto dto = new DishDto();

        when(dishService.createDish(dto))
                .thenReturn(dto);

        ResponseEntity<DishDto> response =
                controller.createDish(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(dishService).createDish(dto);
    }

    @Test
    void shouldGetDishTypes() {
        when(dishService.getDishType())
                .thenReturn(List.of(
                        DishType.MEAT,
                        DishType.FISH,
                        DishType.VEGETARIAN
                ));

        ResponseEntity<List<String>> response =
                controller.getDishTypes();

        assertEquals(200, response.getStatusCode().value());

        assertEquals(
                List.of("MEAT", "FISH", "VEGETARIAN"),
                response.getBody()
        );

        verify(dishService).getDishType();
    }

    @Test
    void shouldGenerateDishInformation() {
        GetDishDTO dto = new GetDishDTO();

        when(dishService.generateDishInformation(dto))
                .thenReturn(dto);

        ResponseEntity<GetDishDTO> response =
                controller.generateDishInformation(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(dishService).generateDishInformation(dto);
    }

    @Test
    void shouldGetAlternatives() {
        List<DishDto> alternatives = List.of(
                new DishDto(),
                new DishDto()
        );

        when(dishService.getAlternatives(1L))
                .thenReturn(alternatives);

        ResponseEntity<List<DishDto>> response =
                controller.getAlternatives(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(alternatives, response.getBody());

        verify(dishService).getAlternatives(1L);
    }

    @Test
    void shouldReplaceDish() {
        ResponseEntity<Void> response =
                controller.replaceDish(1L, 2L);

        assertEquals(204, response.getStatusCode().value());

        verify(dishService).replaceDish(1L, 2L);
    }

    @Test
    void shouldGetOrganicProducts() {
        when(dishService.getOrganicProducts())
                .thenReturn(75.5);

        ResponseEntity<Double> response =
                controller.getOrganicProducts();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(75.5, response.getBody());

        verify(dishService).getOrganicProducts();
    }
}