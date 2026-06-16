package bioCanteenApp.products.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;

import java.util.List;

public interface IProductService {
    List<ProductDTO> getAllProducts();

    List<ProductDTO> getAvailableSeasonalProducts();

    double calculateOrganicProductsPercentage();

    Long getProductCount();
}
