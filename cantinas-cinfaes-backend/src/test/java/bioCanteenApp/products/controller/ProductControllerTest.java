package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private IProductService productService;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        productService = mock(IProductService.class);

        controller = new ProductController(productService);
    }

    @Test
    void shouldGetAllProducts() {
        List<ProductDTO> products = List.of(
                new ProductDTO(),
                new ProductDTO()
        );

        when(productService.getAllProducts())
                .thenReturn(products);

        ResponseEntity<List<ProductDTO>> response =
                controller.getAllProducts();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(products, response.getBody());

        verify(productService).getAllProducts();
    }

    @Test
    void shouldGetSeasonalProducts() {
        List<ProductDTO> products = List.of(
                new ProductDTO(),
                new ProductDTO()
        );

        when(productService.getAvailableSeasonalProducts())
                .thenReturn(products);

        ResponseEntity<List<ProductDTO>> response =
                controller.getSeasonalProducts();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(products, response.getBody());

        verify(productService)
                .getAvailableSeasonalProducts();
    }

    @Test
    void shouldGetProductCount() {
        when(productService.getProductCount())
                .thenReturn(50L);

        ResponseEntity<Long> response =
                controller.getProductCount();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(50L, response.getBody());

        verify(productService).getProductCount();
    }
}