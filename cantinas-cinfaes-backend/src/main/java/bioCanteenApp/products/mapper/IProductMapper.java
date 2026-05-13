package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductDTO;

public interface IProductMapper {
    Product toDomain(ProductDTO dto);

    ProductDTO toDTO(Product product);
}
