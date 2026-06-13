package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.repository.IProductBatchRepo;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBatchMapperTest {

    @Mock
    private IProductMapper productMapper;

    @Mock
    private IProductBatchRepo productBatchRepository;

    @Mock
    private IProductRepo productRepository;

    @Mock
    private ISupplierRepo supplierRepository;

    @InjectMocks
    private ProductBatchMapper mapper;

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFields() {
        ProductBatch batch = mock(ProductBatch.class);
        when(batch.getId()).thenReturn(10L);
        when(batch.getQuantity()).thenReturn(50.0);
        when(batch.getReceivedDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(batch.getExpirationDate()).thenReturn(LocalDate.of(2026, 6, 1));

        ProductBatchDTO dto = mapper.toDTO(batch);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals(10L, dto.getProductId());
        assertEquals(50.0, dto.getQuantity());
        assertEquals(LocalDate.of(2026, 1, 1), dto.getReceivedDate());
        assertEquals(LocalDate.of(2026, 6, 1), dto.getExpirationDate());
    }

    @Test
    void toDomain_withNull_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_whenBatchExistsInRepo_returnsExisting() {
        ProductBatch existing = mock(ProductBatch.class);
        when(productBatchRepository.findById(5L)).thenReturn(existing);

        ProductBatchDTO dto = ProductBatchDTO.builder().id(5L).build();

        ProductBatch result = mapper.toDomain(dto);

        assertSame(existing, result);
    }

    @Test
    void toDomain_whenProductNotFound_throwsException() {
        when(productBatchRepository.findById(1L)).thenReturn(null);
        when(productRepository.findById(99L)).thenReturn(null);

        ProductBatchDTO dto = ProductBatchDTO.builder()
                .id(1L).productId(99L).supplierId(1L).build();

        assertThrows(IllegalArgumentException.class, () -> mapper.toDomain(dto));
    }

    @Test
    void toDomain_whenSupplierNotFound_throwsException() {
        Product product = mock(Product.class);
        when(productBatchRepository.findById(1L)).thenReturn(null);
        when(productRepository.findById(10L)).thenReturn(product);
        when(supplierRepository.findById(99L)).thenReturn(null);

        ProductBatchDTO dto = ProductBatchDTO.builder()
                .id(1L).productId(10L).supplierId(99L).build();

        assertThrows(IllegalArgumentException.class, () -> mapper.toDomain(dto));
    }

    @Test
    void toDomain_success_createsProductBatch() {
        Product product = mock(Product.class);
        Supplier supplier = mock(Supplier.class);
        when(product.getExpirationDays()).thenReturn(30);
        when(productBatchRepository.findById(1L)).thenReturn(null);
        when(productRepository.findById(10L)).thenReturn(product);
        when(supplierRepository.findById(20L)).thenReturn(supplier);

        ProductBatchDTO dto = ProductBatchDTO.builder()
                .id(1L)
                .productId(10L)
                .supplierId(20L)
                .quantity(100.0)
                .receivedDate(LocalDate.of(2026, 1, 1))
                .build();

        ProductBatch result = mapper.toDomain(dto);

        assertNotNull(result);
        assertEquals(product, result.getProduct());
        assertEquals(supplier, result.getSupplier());
        assertEquals(100.0, result.getQuantity());
    }
}
