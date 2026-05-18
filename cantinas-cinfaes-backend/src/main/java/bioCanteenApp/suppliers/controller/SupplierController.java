package bioCanteenApp.suppliers.controller;

import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")
@Slf4j
public class SupplierController {

    private final ISupplierService supplierService;

    @PostMapping("/apply")
    public ResponseEntity<SupplierApplicationDTO> applyToSupplierPosition(
            @RequestBody SupplierApplicationDTO dto
    ) {

        log.info(
                "Supplier application submitted for company: {}",
                dto.getName()
        );

        SupplierApplicationDTO createdApplication =
                supplierService.applyToSupplierPosition(dto);

        log.info(
                "Supplier application created successfully with id: {}",
                createdApplication.getId()
        );

        return ResponseEntity.ok(createdApplication);
    }

    @PostMapping("/approval")
    public ResponseEntity<SupplierDTO> approveSupplier(
            @RequestBody SupplierDTO dto
    ) {

        log.warn(
                "Approving supplier with name: {}",
                dto.getName()
        );

        SupplierDTO supplier =
                supplierService.approveSupplier(dto);

        log.info(
                "Supplier approved successfully with application id: {}",
                supplier.getApplicationId()
        );

        return ResponseEntity.ok(supplier);
    }

    @PostMapping("/reject")
    public ResponseEntity<SupplierDTO> rejectSupplier(
            @RequestBody SupplierDTO dto
    ) {

        log.warn(
                "Rejecting supplier with name: {}",
                dto.getName()
        );

        SupplierDTO supplier =
                supplierService.rejectSupplier(dto);

        log.info(
                "Supplier rejected successfully with application id: {}",
                supplier.getApplicationId()
        );

        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getSupplierStats() {

        log.info("Fetching supplier statistics");

        Map<String, Long> stats =
                supplierService.getSupplierStats();

        log.info("Supplier statistics fetched successfully");

        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> findAllSuppliers() {

        log.info("Fetching all suppliers");

        List<SupplierDTO> suppliers =
                supplierService.findAllSuppliers();

        log.info("Found {} suppliers", suppliers.size());

        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<SupplierApplicationDTO>> findAllApplications() {

        log.info("Fetching all supplier applications");

        List<SupplierApplicationDTO> applications =
                supplierService.findAllApplications();

        log.info(
                "Found {} supplier applications",
                applications.size()
        );

        return ResponseEntity.ok(applications);
    }

    @GetMapping("/order/{productId}")
    public ResponseEntity<List<SupplierDTO>> findAllSuppliersByOrderByProduct(
            @PathVariable("productId") Long id
    ) {

        log.info(
                "Fetching suppliers ordered by product id: {}",
                id
        );

        List<SupplierDTO> suppliers =
                supplierService.findAllSuppliersByOrderByProduct(id);

        log.info(
                "Found {} suppliers for product id: {}",
                suppliers.size(),
                id
        );

        return ResponseEntity.ok(suppliers);
    }

    @PostMapping("/quarantine")
    public ResponseEntity<SupplierDTO> quarantineSupplier(
            @RequestBody SupplierDTO request
    ) {

        log.warn(
                "Quarantining supplier with application id: {}",
                request.getApplicationId()
        );

        SupplierDTO dto =
                supplierService.quarantineSupplier(request);

        log.info(
                "Supplier quarantined successfully with application id: {}",
                dto.getApplicationId()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/unquarantine")
    public ResponseEntity<SupplierDTO> unquarantineSupplier(
            @RequestBody SupplierDTO request
    ) {

        log.warn(
                "Removing quarantine from supplier with application id: {}",
                request.getApplicationId()
        );

        SupplierDTO dto =
                supplierService.unquarantineSupplier(request);

        log.info(
                "Supplier unquarantined successfully with application id: {}",
                dto.getApplicationId()
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/name/{name}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByName(
            @PathVariable("name") String name
    ) {

        log.info("Filtering suppliers by name: {}", name);

        List<SupplierDTO> dto =
                supplierService.getSuppliersByName(name);

        log.info(
                "Found {} suppliers with name filter: {}",
                dto.size(),
                name
        );

    ResponseEntity<List<SupplierDTO>> getSuppliersByName(@PathVariable("name") String name) {
        List<SupplierDTO> dto = supplierService.getSuppliersByName(name);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/village/{village}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByVillage(
            @PathVariable("village") String village
    ) {

        log.info("Filtering suppliers by village: {}", village);

        List<SupplierDTO> dto =
                supplierService.getSuppliersByVillage(village);

        log.info(
                "Found {} suppliers in village: {}",
                dto.size(),
                village
        );

    ResponseEntity<List<SupplierDTO>> getSuppliersByVillage(@PathVariable("village") String village) {
        List<SupplierDTO> dto = supplierService.getSuppliersByVillage(village);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/municipality/{municipality}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByMunicipality(
            @PathVariable("municipality") String village
    ) {

        log.info("Filtering suppliers by municipality: {}", village);

        List<SupplierDTO> dto =
                supplierService.getSuppliersByMunicipality(village);

        log.info(
                "Found {} suppliers in municipality: {}",
                dto.size(),
                village
        );

    ResponseEntity<List<SupplierDTO>> getSuppliersByMunicipality(@PathVariable("municipality") String village) {
        List<SupplierDTO> dto = supplierService.getSuppliersByMunicipality(village);
        return ResponseEntity.ok(dto);
    }
}