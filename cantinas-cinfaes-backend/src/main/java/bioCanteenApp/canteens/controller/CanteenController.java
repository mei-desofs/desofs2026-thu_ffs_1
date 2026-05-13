package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.service.ICanteenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canteens")
public class CanteenController {

    private final ICanteenService service;

    public CanteenController(ICanteenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CanteenDTO> createCanteen(@RequestBody CanteenDTO request) {
        CanteenDTO created = service.createCanteen(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CanteenDTO>> getAllCanteens() {
        return ResponseEntity.ok(service.getAllCanteens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanteenDTO> getCanteenById(@PathVariable("id") Long id) {
        CanteenDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/quarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> quarantineVillage(@PathVariable("village") String village) {
        List<CanteenDTO> quarantined = service.quarantineCanteensByVillage(village);
        return ResponseEntity.ok(quarantined);
    }

    @PostMapping("/unquarantine/{village}")
    public ResponseEntity<List<CanteenDTO>> unquarantineVillage(@PathVariable("village") String village) {
        List<CanteenDTO> unquarantined = service.unquarantineCanteensByVillage(village);
        return ResponseEntity.ok(unquarantined);
    }

    @GetMapping("/filter/{municipality}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByMunicipality(@PathVariable("municipality") String municipality) {
        List<CanteenDTO> dto=service.getByMunicipality(municipality);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/{village}")
    public ResponseEntity<List<CanteenDTO>> getCanteensByVillage(@PathVariable("village") String village) {
        List<CanteenDTO> dto=service.getByVillage(village);
        return ResponseEntity.ok(dto);
    }


}
