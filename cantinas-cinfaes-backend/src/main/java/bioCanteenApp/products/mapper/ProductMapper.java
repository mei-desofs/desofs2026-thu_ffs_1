package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.repository.IProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper implements IProductMapper {

    private final IProductRepo productRepo;

    @Override
    public Product toDomain(ProductDTO dto) {

        Product product = productRepo.findById(dto.getId());

        if(product != null) {
            return product;
        }

        List<Season> seasons = dto.getSeasonalSeasons() != null
                ? dto.getSeasonalSeasons().stream()
                .map(String::toUpperCase)
                .map(Season::valueOf)
                .collect(Collectors.toList())
                : null;

        List<Allergen> allergens = dto.getAllergens() != null
                ? dto.getAllergens().stream()
                .map(String::toUpperCase)
                .map(Allergen::valueOf)
                .collect(Collectors.toList())
                : null;

        return new Product(
                dto.getName(),
                dto.getUnit(),
                dto.getExpirationDays(),
                seasons,
                allergens
        );

    }

    @Override
    public ProductDTO toDTO(Product product) {
        List<String> seasons = product.getSeasons() != null
                ? product.getSeasons().stream()
                .map(Enum::name)
                .collect(Collectors.toList())
                : null;

        List<String> allergens = product.getAllergens() != null
                ? product.getAllergens().stream()
                .map(Enum::name)
                .collect(Collectors.toList())
                : null;

        return ProductDTO.builder()
                .id(product.getId() != null ? product.getId() : 0)
                .name(product.getName())
                .unit(product.getUnit())
                .expirationDays(product.getExpirationDays())
                .seasonalSeasons(seasons)
                .allergens(allergens)
                .build();

    }
}
