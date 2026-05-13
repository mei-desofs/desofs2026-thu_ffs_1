package bioCanteenApp.diningHall.controller;

import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.service.IDiningHallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dining-halls")
public class DiningHallController {

    private final IDiningHallService service;

    public DiningHallController(IDiningHallService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DiningHallDTO>> getAllDiningHalls() {
        return ResponseEntity.ok(service.getAllDiningHall());
    }

}
