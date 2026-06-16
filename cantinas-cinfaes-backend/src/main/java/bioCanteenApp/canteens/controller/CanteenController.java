package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.service.ICanteenService;
import bioCanteenApp.utils.exceptions.LogSanitizer;
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

    @GetMapping
    public ResponseEntity<CanteenDTO> createCanteen(@RequestBody CanteenDTO request) {

        log.info("Creating canteen with name: {}", LogSanitizer.sanitize(request.getName()));

        CanteenDTO created = service.createCanteen(request);

        log.info("Canteen created successfully with name: {}", LogSanitizer.sanitize(created.getName()));

        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CanteenDTO>> getAllCanteens() {

        log.info("Fetching all canteens");

        List<CanteenDTO> canteens = service.getAllCanteens();

        log.info("Found {} canteens", canteens.size());

        return ResponseEntity.ok(canteens);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CanteenDTO> getCanteenById(@PathVariable("id") Long id) {

        log.info("Fetching canteen with id: {}", LogSanitizer.sanitize(id));

        CanteenDTO dto = service.getById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/quarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> quarantineVillage(
            @PathVariable("village") String village
    ) {

        log.warn("Quarantining canteens in village: {}", village);

        List<CanteenDTO> quarantined =
                service.quarantineCanteensByVillage(village);

        log.info(
                "Quarantined {} canteens in village: {}",
                quarantined.size(),
                LogSanitizer.sanitize(village)
        );

        return ResponseEntity.ok(quarantined);
    }

    @PostMapping(value = "/unquarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> unquarantineVillage(
            @PathVariable("village") String village
    ) {

        log.warn("Removing quarantine from canteens in village: {}", village);

        List<CanteenDTO> unquarantined =
                service.unquarantineCanteensByVillage(village);

        log.info(
                "Unquarantined {} canteens in village: {}",
                unquarantined.size(),
                LogSanitizer.sanitize(village)
        );

        return ResponseEntity.ok(unquarantined);
    }

    @GetMapping(value = "/filter/municipality/{municipality}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByMunicipality(
            @PathVariable("municipality") String municipality
    ) {

        log.info("Filtering canteens by municipality: {}", municipality);

        List<CanteenDTO> dto =
                service.getByMunicipality(municipality);

        log.info(
                "Found {} canteens in municipality: {}",
                dto.size(),
                LogSanitizer.sanitize(municipality)
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/filter/village/{village}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByVillage(
            @PathVariable("village") String village
    ) {

        log.info("Filtering canteens by village: {}", village);

        List<CanteenDTO> dto =
                service.getByVillage(village);

        log.info(
                "Found {} canteens in village: {}",
                dto.size(),
                LogSanitizer.sanitize(village)
        );

        return ResponseEntity.ok(dto);
    }
}