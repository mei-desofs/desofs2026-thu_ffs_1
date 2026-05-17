package bioCanteenApp.products.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.mapper.ProductMapper;
import bioCanteenApp.products.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private ProductRepo productRepository;
    private ProductMapper productMapper;

    private ProductService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepo.class);
        productMapper = mock(ProductMapper.class);

        service = new ProductService(
                productRepository,
                productMapper
        );
    }

    @Test
    void shouldGetAllProducts() {
        Product product1 = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Product product2 = new Product(
                "Fish",
                "kg",
                5,
                List.of(Season.SUMMER),
                List.of(Allergen.FISH)
        );

        ProductDTO dto1 = ProductDTO.builder()
                .id(1L)
                .name("Rice")
                .unit("kg")
                .expirationDays(365)
                .seasonalSeasons(List.of(Season.SPRING.name()))
                .allergens(List.of(Allergen.GLUTEN.name()))
                .build();

        ProductDTO dto2 = ProductDTO.builder()
                .id(2L)
                .name("Fish")
                .unit("kg")
                .expirationDays(5)
                .seasonalSeasons(List.of(Season.SUMMER.name()))
                .allergens(List.of(Allergen.FISH.name()))
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(productMapper.toDTO(product1)).thenReturn(dto1);
        when(productMapper.toDTO(product2)).thenReturn(dto2);

        List<ProductDTO> result = service.getAllProducts();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(productMapper).toDTO(product1);
        verify(productMapper).toDTO(product2);
    }

    @Test
    void shouldReturnZeroForOrganicProductsPercentage() {
        Dish dish = new Dish("Vegetarian Dish", DishType.VEGETARIAN);

        double result = service.calculateOrganicProductsPercentage(dish);

        assertEquals(0.0, result);
    }

    @Test
    void shouldGetProductCount() {
        when(productRepository.count()).thenReturn(5L);

        Long result = service.getProductCount();

        assertEquals(5L, result);
        verify(productRepository).count();
    }
}