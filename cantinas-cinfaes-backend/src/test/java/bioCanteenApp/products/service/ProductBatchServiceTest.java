package bioCanteenApp.products.service;

import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.mapper.IProductBatchMapper;
import bioCanteenApp.products.repository.IProductBatchRepo;
import bioCanteenApp.products.repository.IProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductBatchServiceTest {

    private IProductBatchRepo productBatchRepository;
    private IProductBatchMapper productBatchMapper;
    private IProductRepo productRepo;

    private ProductBatchService service;

    @BeforeEach
    void setUp() {
        productBatchRepository = mock(IProductBatchRepo.class);
        productBatchMapper = mock(IProductBatchMapper.class);
        productRepo = mock(IProductRepo.class);

        service = new ProductBatchService(
                productBatchMapper,
                productBatchRepository,
                productRepo
        );
    }

    @Test
    void shouldGetAllBatches() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch1 = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);
        ProductBatch batch2 = new ProductBatch(product, 20.0, LocalDate.of(2026, 5, 15), false, null);

        ProductBatchDTO dto1 = null;
        ProductBatchDTO dto2 = null;

        when(productBatchRepository.findAll()).thenReturn(List.of(batch1, batch2));
        when(productBatchMapper.toDTO(batch1)).thenReturn(dto1);
        when(productBatchMapper.toDTO(batch2)).thenReturn(dto2);

        List<ProductBatchDTO> result = service.getAllBatches();

        assertEquals(2, result.size());
        verify(productBatchMapper).toDTO(batch1);
        verify(productBatchMapper).toDTO(batch2);
    }

    @Test
    void shouldGetBatchById() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);

        when(productBatchRepository.findById(1L)).thenReturn(batch);
        when(productBatchMapper.toDTO(batch)).thenReturn(null);

        ProductBatchDTO result = service.getBatchById(1L);

        assertNull(result);
        verify(productBatchRepository).findById(1L);
        verify(productBatchMapper).toDTO(batch);
    }

    @Test
    void shouldGetBatchesByProduct() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);

        when(productRepo.findById(1L)).thenReturn(product);
        when(productBatchRepository.findByProduct(product)).thenReturn(List.of(batch));
        when(productBatchMapper.toDTO(batch)).thenReturn(null);

        List<ProductBatchDTO> result = service.getBatchesByProduct(1L);

        assertEquals(1, result.size());
        verify(productRepo).findById(1L);
        verify(productBatchRepository).findByProduct(product);
        verify(productBatchMapper).toDTO(batch);
    }

    @Test
    void shouldThrowWhenProductDoesNotExistOnGetBatchesByProduct() {
        when(productRepo.findById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBatchesByProduct(1L)
        );

        verify(productBatchRepository, never()).findByProduct(any());
    }

    @Test
    void shouldGetValidBatchesByProduct() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);

        when(productRepo.findById(1L)).thenReturn(product);
        when(productBatchRepository.findValidBatchesByProduct(product)).thenReturn(List.of(batch));
        when(productBatchMapper.toDTO(batch)).thenReturn(null);

        List<ProductBatchDTO> result = service.getValidBatchesByProduct(1L);

        assertEquals(1, result.size());
        verify(productRepo).findById(1L);
        verify(productBatchRepository).findValidBatchesByProduct(product);
        verify(productBatchMapper).toDTO(batch);
    }

    @Test
    void shouldThrowWhenProductDoesNotExistOnGetValidBatchesByProduct() {
        when(productRepo.findById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getValidBatchesByProduct(1L)
        );

        verify(productBatchRepository, never()).findValidBatchesByProduct(any());
    }

    @Test
    void shouldGetValidStockByProduct() {
        when(productBatchRepository.sumValidStockByProduct(1L)).thenReturn(50.0);

        double result = service.getValidStockByProduct(1L);

        assertEquals(50.0, result);
        verify(productBatchRepository).sumValidStockByProduct(1L);
    }

    @Test
    void shouldSaveBatch() {
        ProductBatchDTO dto = null;

        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);
        ProductBatch savedBatch = new ProductBatch(product, 20.0, LocalDate.of(2026, 5, 14), true, null);

        when(productBatchMapper.toDomain(dto)).thenReturn(batch);
        when(productBatchRepository.save(batch)).thenReturn(savedBatch);
        when(productBatchMapper.toDTO(savedBatch)).thenReturn(null);

        ProductBatchDTO result = service.saveBatch(dto);

        assertNull(result);
        verify(productBatchMapper).toDomain(dto);
        verify(productBatchRepository).save(batch);
        verify(productBatchMapper).toDTO(savedBatch);
    }

    @Test
    void shouldDeleteBatch() {
        ProductBatchDTO dto = null;

        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        ProductBatch batch = new ProductBatch(product, 10.0, LocalDate.of(2026, 5, 14), true, null);

        when(productBatchMapper.toDomain(dto)).thenReturn(batch);

        service.deleteBatch(dto);

        verify(productBatchMapper).toDomain(dto);
        verify(productBatchRepository).delete(batch);
    }
}