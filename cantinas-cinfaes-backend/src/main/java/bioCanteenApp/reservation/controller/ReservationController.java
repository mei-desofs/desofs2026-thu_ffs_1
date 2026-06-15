package bioCanteenApp.reservation.controller;

import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.service.IReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
public class ReservationController {

    private final IReservationService service;

    public ReservationController(IReservationService service) {
        this.service = service;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationDTO> createReservation(
            @RequestBody ReservationDTO request
    ) {

        log.info(
                "Creating reservation for user id: {}",
                request.getUserId()
        );

        ReservationDTO created =
                service.createReservation(request);

        log.info(
                "Reservation created successfully with id: {}",
                created.getUserId()
        );

        return ResponseEntity.ok(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {

        log.info("Fetching all reservations");

        List<ReservationDTO> reservations =
                service.getAllReservations();

        log.info(
                "Found {} reservations",
                reservations.size()
        );

        return ResponseEntity.ok(reservations);
    }

    @GetMapping(value = "/{id}", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationDTO> getReservationById(
            @PathVariable("id") Long id
    ) {

        log.info("Fetching reservation with id: {}", id);

        ReservationDTO dto =
                service.getById(id);

        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/user/{userId}", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(
            @PathVariable("userId") Long userId
    ) {

        log.info(
                "Fetching reservations for user id: {}",
                userId
        );

        List<ReservationDTO> reservations =
                service.getByUserId(userId);

        log.info(
                "Found {} reservations for user id: {}",
                reservations.size(),
                userId
        );

        return ResponseEntity.ok(reservations);
    }
}