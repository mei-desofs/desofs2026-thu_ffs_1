package bioCanteenApp.suppliers.controller;

import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.service.ISupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final ISupplierService supplierService;

    // UC3: Send a supplier application (REQ3.1, REQ3.2, REQ3.3)
    @PostMapping(value = "/apply", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SupplierApplicationDTO> applyToSupplierPosition(
            @Valid @RequestPart("application") SupplierApplicationDTO dto,
            @RequestPart("certificate") MultipartFile certificate) {

        return ResponseEntity.ok(supplierService.applyToSupplierPosition(dto, certificate));
    }

    // UC4: Approve an application (REQ4.1, REQ4.2, REQ4.3)
    @PostMapping("/approve/{id}")
    public ResponseEntity<SupplierDTO> approveSupplier(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supplierService.approveSupplier(id));
    }

    // UC4: Reject an application (REQ4.4)
    @PostMapping("/reject/{id}")
    public ResponseEntity<SupplierDTO> rejectSupplier(
            @PathVariable("id") Long id,
            @RequestBody String reason) {
        return ResponseEntity.ok(supplierService.rejectSupplier(id, reason));
    }

    // --- Restantes métodos inalterados ---

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getSupplierStats() {
        return ResponseEntity.ok(supplierService.getSupplierStats());
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> findAllSuppliers() {
        return ResponseEntity.ok(supplierService.findAllSuppliers());
    }

    @GetMapping("/applications")
    public ResponseEntity<List<SupplierApplicationDTO>> findAllApplications() {
        return ResponseEntity.ok(supplierService.findAllApplications());
    }

    @GetMapping("/order/{productId}")
    public ResponseEntity<List<SupplierDTO>> findAllSuppliersByOrderByProduct(@PathVariable("productId") Long id) {
        return ResponseEntity.ok(supplierService.findAllSuppliersByOrderByProduct(id));
    }

    @PostMapping("/quarantine")
    public ResponseEntity<SupplierDTO> quarantineSupplier(@RequestBody SupplierDTO request) {
        return ResponseEntity.ok(supplierService.quarantineSupplier(request));
    }

    @PostMapping("/unquarantine")
    public ResponseEntity<SupplierDTO> unquarantineSupplier(@RequestBody SupplierDTO request) {
        return ResponseEntity.ok(supplierService.unquarantineSupplier(request));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SupplierDTO>> filterSuppliers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "village", required = false) String village,
            @RequestParam(name = "municipality", required = false) String municipality) {

        if (name != null) return ResponseEntity.ok(supplierService.getSuppliersByName(name));
        if (village != null) return ResponseEntity.ok(supplierService.getSuppliersByVillage(village));
        if (municipality != null) return ResponseEntity.ok(supplierService.getSuppliersByMunicipality(municipality));

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/application/{id}/certificate")
    public ResponseEntity<byte[]> getBioCertificate(@PathVariable("id") Long id) {
        byte[] certificate = supplierService.getBioCertificate(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bio_certificate_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(certificate);
    }
}