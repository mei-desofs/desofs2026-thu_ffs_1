package bioCanteenApp.reservation.controller;

import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.service.IReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final IReservationService service;

    public ReservationController(IReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody ReservationDTO request) {
        ReservationDTO created = service.createReservation(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(service.getAllReservations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable("id") Long id) {
        ReservationDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
