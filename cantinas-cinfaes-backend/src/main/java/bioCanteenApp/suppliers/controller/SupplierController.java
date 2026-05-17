package bioCanteenApp.suppliers.controller;

import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final ISupplierService supplierService;

    // POST Apply as Supplier
    @PostMapping("/apply")
    public ResponseEntity<SupplierApplicationDTO> applyToSupplierPosition(@RequestBody SupplierApplicationDTO dto) {
        SupplierApplicationDTO createdApplication = supplierService.applyToSupplierPosition(dto);
        return ResponseEntity.ok(createdApplication);
    }

    // POST create new supplier
    @PostMapping("/approval")
    public ResponseEntity<SupplierDTO> approveSupplier(@RequestBody SupplierDTO dto) {
        SupplierDTO supplier = supplierService.approveSupplier(dto);
        return ResponseEntity.ok(supplier);
    }

    @PostMapping("/reject")
    public ResponseEntity<SupplierDTO> rejectSupplier(@RequestBody SupplierDTO dto) {
        SupplierDTO supplier = supplierService.rejectSupplier(dto);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getSupplierStats() {
        Map<String, Long> stats = supplierService.getSupplierStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> findAllSuppliers() {
        return ResponseEntity.ok(supplierService.findAllSuppliers());
    }

    @GetMapping("/applications")
    public ResponseEntity<List<SupplierApplicationDTO>> findAllApplications() {
        return ResponseEntity.ok(supplierService.findAllApplications());
    }

    //id do product no mapping
    @GetMapping("/order/{productId}")
    public ResponseEntity<List<SupplierDTO>> findAllSuppliersByOrderByProduct(@PathVariable("productId") Long id) {
        List<SupplierDTO> suppliers = supplierService.findAllSuppliersByOrderByProduct(id);
        return ResponseEntity.ok(suppliers);
    }

    @PostMapping("/quarantine")
    public ResponseEntity<SupplierDTO> quarantineSupplier(@RequestBody SupplierDTO request) {
        SupplierDTO dto = supplierService.quarantineSupplier(request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/unquarantine")
    public ResponseEntity<SupplierDTO> unquarantineSupplier(@RequestBody SupplierDTO request) {
        SupplierDTO dto = supplierService.unquarantineSupplier(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/name/{name}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByName(@PathVariable("name") String name) {
        List<SupplierDTO> dto = supplierService.getSuppliersByName(name);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/village/{village}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByVillage(@PathVariable("village") String village) {
        List<SupplierDTO> dto = supplierService.getSuppliersByVillage(village);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter/municipality/{municipality}")
    ResponseEntity<List<SupplierDTO>> getSuppliersByMunicipality(@PathVariable("municipality") String village) {
        List<SupplierDTO> dto = supplierService.getSuppliersByMunicipality(village);
        return ResponseEntity.ok(dto);
    }
}