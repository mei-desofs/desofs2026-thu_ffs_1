package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {

        log.info("Fetching all products");

        List<ProductDTO> products =
                productService.getAllProducts();

        log.info("Found {} products", products.size());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/seasonal")
    public ResponseEntity<List<ProductDTO>> getSeasonalProducts() {

        log.info("Fetching seasonal products");

        List<ProductDTO> products =
                productService.getAvailableSeasonalProducts();

        log.info(
                "Found {} seasonal products",
                products.size()
        );

        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Long> getProductCount() {

        log.info("Fetching product statistics");

        Long count =
                productService.getProductCount();

        log.info("Current product count: {}", count);

        return ResponseEntity.ok(count);
    }
}