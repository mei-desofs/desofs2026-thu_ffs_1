package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.repository.IProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private IProductRepo productRepo;

    @InjectMocks
    private ProductMapper mapper;

    @Test
    void toDTO_mapsAllFields() {
        Product product = new Product("Cenoura", "kg", 7, List.of(Season.AUTUMN, Season.WINTER), List.of(Allergen.GLUTEN));
        product.setId(1L);

        ProductDTO dto = mapper.toDTO(product);

        assertEquals(1L, dto.getId());
        assertEquals("Cenoura", dto.getName());
        assertEquals("kg", dto.getUnit());
        assertEquals(7, dto.getExpirationDays());
        assertEquals(List.of("AUTUMN", "WINTER"), dto.getSeasonalSeasons());
        assertEquals(List.of("GLUTEN"), dto.getAllergens());
    }

    @Test
    void toDTO_withNullSeasonsAndAllergens_mapsNulls() {
        Product product = new Product("Arroz", "g", 365, null, null);

        ProductDTO dto = mapper.toDTO(product);

        assertEquals("Arroz", dto.getName());
        assertNull(dto.getSeasonalSeasons());
        assertNull(dto.getAllergens());
    }

    @Test
    void toDTO_withNullId_mapsZero() {
        Product product = new Product("Batata", "kg", 10, null, null);

        ProductDTO dto = mapper.toDTO(product);

        assertEquals(0L, dto.getId());
    }

    @Test
    void toDomain_whenProductExistsInRepo_returnsExisting() {
        Product existing = new Product("Existente", "kg", 5, null, null);
        when(productRepo.findById(1L)).thenReturn(existing);

        ProductDTO dto = ProductDTO.builder().id(1L).name("Other").build();

        Product result = mapper.toDomain(dto);

        assertSame(existing, result);
        verify(productRepo).findById(1L);
    }

    @Test
    void toDomain_whenProductNotInRepo_createsNewProduct() {
        when(productRepo.findById(99L)).thenReturn(null);

        ProductDTO dto = ProductDTO.builder()
                .id(99L)
                .name("Novo")
                .unit("L")
                .expirationDays(30)
                .seasonalSeasons(List.of("SPRING", "SUMMER"))
                .allergens(List.of("MILK", "EGGS"))
                .build();

        Product result = mapper.toDomain(dto);

        assertNotNull(result);
        assertEquals("Novo", result.getName());
        assertEquals("L", result.getUnit());
        assertEquals(30, result.getExpirationDays());
        assertEquals(List.of(Season.SPRING, Season.SUMMER), result.getSeasons());
        assertEquals(List.of(Allergen.MILK, Allergen.EGGS), result.getAllergens());
    }

    @Test
    void toDomain_withNullSeasonsAndAllergens_createsProduct() {
        when(productRepo.findById(2L)).thenReturn(null);

        ProductDTO dto = ProductDTO.builder()
                .id(2L)
                .name("Simples")
                .unit("un")
                .expirationDays(14)
                .seasonalSeasons(null)
                .allergens(null)
                .build();

        Product result = mapper.toDomain(dto);

        assertNotNull(result);
        assertNull(result.getSeasons());
        assertNull(result.getAllergens());
    }
}
