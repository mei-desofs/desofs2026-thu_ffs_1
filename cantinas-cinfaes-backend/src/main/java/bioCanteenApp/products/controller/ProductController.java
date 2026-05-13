package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seasonal")
    public ResponseEntity<List<ProductDTO>> getSeasonalProducts() {
        List<ProductDTO> products = productService.getAvailableSeasonalProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Long> getProductCount() {
        Long count = productService.getProductCount();
        return ResponseEntity.ok(count);
    }

}
