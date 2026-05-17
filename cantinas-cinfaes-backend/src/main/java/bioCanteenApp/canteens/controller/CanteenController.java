package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.service.ICanteenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canteens")
@Slf4j
public class CanteenController {

    private final ICanteenService service;

    public CanteenController(ICanteenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CanteenDTO> createCanteen(@RequestBody CanteenDTO request) {

        log.info("Creating canteen with name: {}", request.getName());

        CanteenDTO created = service.createCanteen(request);

        log.info("Canteen created successfully with name: {}", created.getName());

        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CanteenDTO>> getAllCanteens() {

        log.info("Fetching all canteens");

        return ResponseEntity.ok(service.getAllCanteens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanteenDTO> getCanteenById(@PathVariable("id") Long id) {

        log.info("Fetching canteen with id: {}", id);

        CanteenDTO dto = service.getById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/quarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> quarantineVillage(
            @PathVariable("village") String village
    ) {

        log.warn("Quarantining canteens in village: {}", village);

        List<CanteenDTO> quarantined =
                service.quarantineCanteensByVillage(village);

        log.info(
                "Successfully quarantined {} canteens in village: {}",
                quarantined.size(),
                village
        );

        return ResponseEntity.ok(quarantined);
    }

    @PostMapping("/unquarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> unquarantineVillage(
            @PathVariable("village") String village
    ) {

        log.warn("Removing quarantine from canteens in village: {}", village);

        List<CanteenDTO> unquarantined =
                service.unquarantineCanteensByVillage(village);

        log.info(
                "Successfully unquarantined {} canteens in village: {}",
                unquarantined.size(),
                village
        );

        return ResponseEntity.ok(unquarantined);
    }

    @GetMapping("/filter/municipality/{municipality}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByMunicipality(
            @PathVariable("municipality") String municipality
    ) {

        log.info("Filtering canteens by municipality: {}", municipality);

        List<CanteenDTO> dto =
                service.getByMunicipality(municipality);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/village/{village}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByVillage(
            @PathVariable("village") String village
    ) {

        log.info("Filtering canteens by village: {}", village);

        List<CanteenDTO> dto =
                service.getByVillage(village);

        return ResponseEntity.ok(dto);
    }
}