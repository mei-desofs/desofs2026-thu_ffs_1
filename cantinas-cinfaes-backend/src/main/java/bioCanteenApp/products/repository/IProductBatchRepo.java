package bioCanteenApp.products.repository;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;

import java.util.List;

public interface IProductBatchRepo {
    Iterable<ProductBatch> findAll();

    ProductBatch findById(Long id);

    ProductBatch save(ProductBatch productBatch);

    void delete(ProductBatch productBatch);

    List<ProductBatch> findByProduct(Product product);

    List<ProductBatch> findValidBatchesByProduct(Product product);

    List<ProductBatch> findExpiredBatches();

    double sumValidStockByProduct(Long productId);
}
