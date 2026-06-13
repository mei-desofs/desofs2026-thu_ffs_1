package bioCanteenApp.products.controller;

import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.service.IProductBatchService;
import bioCanteenApp.products.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductBatchControllerTest {

    private IProductBatchService productBatchService;
    private IProductService productService;

    private ProductBatchController controller;

    @BeforeEach
    void setUp() {
        productBatchService = mock(IProductBatchService.class);
        productService = mock(IProductService.class);

        controller = new ProductBatchController(
                productBatchService
        );
    }

    @Test
    void shouldGetAllBatches() {
        List<ProductBatchDTO> batches = List.of(
                new ProductBatchDTO(),
                new ProductBatchDTO()
        );

        when(productBatchService.getAllBatches())
                .thenReturn(batches);

        ResponseEntity<List<ProductBatchDTO>> response =
                controller.getAllBatches();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(batches, response.getBody());

        verify(productBatchService).getAllBatches();
    }

    @Test
    void shouldCreateBatch() {
        ProductBatchDTO dto = new ProductBatchDTO();

        when(productBatchService.saveBatch(dto))
                .thenReturn(dto);

        ResponseEntity<ProductBatchDTO> response =
                controller.createBatch(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(productBatchService).saveBatch(dto);
    }

    @Test
    void shouldGetBatchById() {
        ProductBatchDTO dto = new ProductBatchDTO();

        when(productBatchService.getBatchById(1L))
                .thenReturn(dto);

        ResponseEntity<ProductBatchDTO> response =
                controller.getBatchById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(productBatchService).getBatchById(1L);
    }

    @Test
    void shouldGetBatchesByProduct() {
        List<ProductBatchDTO> batches = List.of(
                new ProductBatchDTO(),
                new ProductBatchDTO()
        );

        when(productBatchService.getBatchesByProduct(1L))
                .thenReturn(batches);

        ResponseEntity<List<ProductBatchDTO>> response =
                controller.getBatchesByProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(batches, response.getBody());

        verify(productBatchService).getBatchesByProduct(1L);
    }

    @Test
    void shouldGetValidBatchesByProduct() {
        List<ProductBatchDTO> batches = List.of(
                new ProductBatchDTO(),
                new ProductBatchDTO()
        );

        when(productBatchService.getValidBatchesByProduct(1L))
                .thenReturn(batches);

        ResponseEntity<List<ProductBatchDTO>> response =
                controller.getValidBatchesByProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(batches, response.getBody());

        verify(productBatchService)
                .getValidBatchesByProduct(1L);
    }

    @Test
    void shouldGetValidStockByProduct() {
        when(productBatchService.getValidStockByProduct(1L))
                .thenReturn(120.5);

        ResponseEntity<Double> response =
                controller.getValidStockByProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(120.5, response.getBody());

        verify(productBatchService)
                .getValidStockByProduct(1L);
    }
}