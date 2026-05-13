package bioCanteenApp.products.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.mapper.ProductMapper;
import bioCanteenApp.products.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepo productRepository;

    private final ProductMapper productMapper;

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = (List<Product>) productRepository.findAll();
        return products.stream().map(productMapper::toDTO).toList();
    }

    @Override
    public List<ProductDTO> getAvailableSeasonalProducts() {
        Month currentMonth = LocalDate.now().getMonth();

        List<Product> seasonalProducts = productRepository.findBySeasonMonthsContaining(currentMonth);

        return seasonalProducts.stream()
                .map(product -> ProductDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .unit(product.getUnit())
                        .expirationDays(product.getExpirationDays())
                        .seasonalSeasons(
                                product.getSeasons().stream()
                                        .map(Enum::name)
                                        .toList()
                        )
                        .allergens(
                                product.getAllergens().stream()
                                        .map(Enum::name)
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }


    public double calculateOrganicProductsPercentage(Dish dish){
        // TODO: Implement this method with new structure for bio products
        /*
        List<Ingredient> ingredients = dish.getIngredients();

        if (ingredients.isEmpty()) return 0.0;

        double organicCount = ingredients.stream()
                .filter(i -> i.getProduct())
                .count();

        return (organicCount / ingredients.size()) * 100;
    }*/
        return 0.0;
    }

    @Override
    public Long getProductCount() {
        return productRepository.count();
    }
}
