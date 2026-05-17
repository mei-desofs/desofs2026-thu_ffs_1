package bioCanteenApp.diningHall.controller;

import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.service.IDiningHallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiningHallControllerTest {

    private IDiningHallService service;
    private DiningHallController controller;

    @BeforeEach
    void setUp() {
        service = mock(IDiningHallService.class);

        controller = new DiningHallController(service);
    }

    @Test
    void shouldGetAllDiningHalls() {
        List<DiningHallDTO> diningHalls = List.of(
                new DiningHallDTO(),
                new DiningHallDTO()
        );

        when(service.getAllDiningHall())
                .thenReturn(diningHalls);

        ResponseEntity<List<DiningHallDTO>> response =
                controller.getAllDiningHalls();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(diningHalls, response.getBody());

        verify(service).getAllDiningHall();
    }
}