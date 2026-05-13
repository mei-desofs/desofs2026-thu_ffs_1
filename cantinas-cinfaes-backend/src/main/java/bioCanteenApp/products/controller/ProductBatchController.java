package bioCanteenApp.products.controller;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.service.IProductBatchService;
import bioCanteenApp.products.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-batches")
public class ProductBatchController {

    private final IProductBatchService productBatchService;
    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductBatchDTO>> getAllBatches() {
        List<ProductBatchDTO> batches = productBatchService.getAllBatches();
        return ResponseEntity.ok(batches);
    }

    @PostMapping
    public ResponseEntity<ProductBatchDTO> createBatch(@RequestBody ProductBatchDTO batchDTO) {
        ProductBatchDTO createdBatch = productBatchService.saveBatch(batchDTO);
        return ResponseEntity.ok(createdBatch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBatchDTO> getBatchById(@PathVariable("id") Long id) {
        ProductBatchDTO batch = productBatchService.getBatchById(id);
        return ResponseEntity.ok(batch);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductBatchDTO>> getBatchesByProduct(
            @PathVariable("productId") Long productId
    ) {

        List<ProductBatchDTO> batches =
                productBatchService.getBatchesByProduct(
                        productId
                );
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/product/{productId}/valid")
    public ResponseEntity<List<ProductBatchDTO>> getValidBatchesByProduct(
            @PathVariable("productId") Long productId
    ) {
        List<ProductBatchDTO> batches =
                productBatchService.getValidBatchesByProduct(
                        productId
                );
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/product/{productId}/stock")
    public ResponseEntity<Double> getValidStockByProduct(
            @PathVariable("productId") Long productId
    ) {
        double stock =
                productBatchService.getValidStockByProduct(productId);
        return ResponseEntity.ok(stock);
    }
}
