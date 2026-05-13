package bioCanteenApp.products.service;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;

import java.util.List;

public interface IProductBatchService {
    List<ProductBatchDTO> getAllBatches();

    ProductBatchDTO getBatchById(Long id);

    List<ProductBatchDTO> getBatchesByProduct(Long product);

    List<ProductBatchDTO> getValidBatchesByProduct(Long product);

    double getValidStockByProduct(Long productId);

    ProductBatchDTO saveBatch(ProductBatchDTO productBatch);

    void deleteBatch(ProductBatchDTO productBatch);

}
