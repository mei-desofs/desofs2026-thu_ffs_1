package bioCanteenApp.reservation.controller;

import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.service.IReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationControllerTest {

    private IReservationService service;
    private ReservationController controller;

    @BeforeEach
    void setUp() {
        service = mock(IReservationService.class);

        controller = new ReservationController(service);
    }

    @Test
    void shouldCreateReservation() {
        ReservationDTO dto = new ReservationDTO();

        when(service.createReservation(dto))
                .thenReturn(dto);

        ResponseEntity<ReservationDTO> response =
                controller.createReservation(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(service).createReservation(dto);
    }

    @Test
    void shouldGetAllReservations() {
        List<ReservationDTO> reservations = List.of(
                new ReservationDTO(),
                new ReservationDTO()
        );

        when(service.getAllReservations())
                .thenReturn(reservations);

        ResponseEntity<List<ReservationDTO>> response =
                controller.getAllReservations();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(reservations, response.getBody());

        verify(service).getAllReservations();
    }

    @Test
    void shouldGetReservationById() {
        ReservationDTO dto = new ReservationDTO();

        when(service.getById(1L))
                .thenReturn(dto);

        ResponseEntity<ReservationDTO> response =
                controller.getReservationById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(service).getById(1L);
    }

    @Test
    void shouldGetReservationsByUserId() {
        List<ReservationDTO> reservations = List.of(
                new ReservationDTO(),
                new ReservationDTO()
        );

        when(service.getByUserId(1L))
                .thenReturn(reservations);

        ResponseEntity<List<ReservationDTO>> response =
                controller.getReservationsByUserId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(reservations, response.getBody());

        verify(service).getByUserId(1L);
    }
}