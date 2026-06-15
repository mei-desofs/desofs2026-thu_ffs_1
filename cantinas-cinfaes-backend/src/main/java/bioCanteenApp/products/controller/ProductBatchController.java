package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.service.IProductBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-batches")
@Slf4j
public class ProductBatchController {

    private final IProductBatchService productBatchService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductBatchDTO>> getAllBatches() {

        log.info("Fetching all product batches");

        List<ProductBatchDTO> batches =
                productBatchService.getAllBatches();

        log.info("Found {} product batches", batches.size());

        return ResponseEntity.ok(batches);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductBatchDTO> createBatch(
            @RequestBody ProductBatchDTO batchDTO
    ) {

        log.info(
                "Creating product batch for product id: {}",
                batchDTO.getProductId()
        );

        ProductBatchDTO createdBatch =
                productBatchService.saveBatch(batchDTO);

        log.info(
                "Product batch created successfully with id: {}",
                createdBatch.getId()
        );

        return ResponseEntity.ok(createdBatch);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductBatchDTO> getBatchById(
            @PathVariable("id") Long id
    ) {

        log.info("Fetching product batch with id: {}", id);

        ProductBatchDTO batch =
                productBatchService.getBatchById(id);

        return ResponseEntity.ok(batch);
    }

    @GetMapping(value = "/product/{productId}", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductBatchDTO>> getBatchesByProduct(
            @PathVariable("productId") Long productId
    ) {

        log.info(
                "Fetching batches for product id: {}",
                productId
        );

        List<ProductBatchDTO> batches =
                productBatchService.getBatchesByProduct(productId);

        log.info(
                "Found {} batches for product id: {}",
                batches.size(),
                productId
        );

        return ResponseEntity.ok(batches);
    }

    @GetMapping(value = "/product/{productId}/valid", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductBatchDTO>> getValidBatchesByProduct(
            @PathVariable("productId") Long productId
    ) {

        log.info(
                "Fetching valid batches for product id: {}",
                productId
        );

        List<ProductBatchDTO> batches =
                productBatchService.getValidBatchesByProduct(productId);

        log.info(
                "Found {} valid batches for product id: {}",
                batches.size(),
                productId
        );

        return ResponseEntity.ok(batches);
    }

    @GetMapping(value = "/product/{productId}/stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getValidStockByProduct(
            @PathVariable("productId") Long productId
    ) {

        log.info(
                "Fetching valid stock for product id: {}",
                productId
        );

        double stock =
                productBatchService.getValidStockByProduct(productId);

        log.info(
                "Valid stock for product id {} is {}",
                productId,
                stock
        );

        return ResponseEntity.ok(stock);
    }
}