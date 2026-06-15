package bioCanteenApp.diningHall.controller;

import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.service.IDiningHallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dining-halls")
@Slf4j
public class DiningHallController {

    private final IDiningHallService service;

    public DiningHallController(IDiningHallService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DiningHallDTO>> getAllDiningHalls() {

        log.info("Fetching all dining halls");

        List<DiningHallDTO> diningHalls =
                service.getAllDiningHall();

        log.info("Found {} dining halls", diningHalls.size());

        return ResponseEntity.ok(diningHalls);
    }
}