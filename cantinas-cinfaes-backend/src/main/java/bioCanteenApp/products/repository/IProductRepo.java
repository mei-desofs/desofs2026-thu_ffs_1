package bioCanteenApp.products.repository;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.suppliers.domain.Supplier;

import java.time.Month;
import java.util.List;

public interface IProductRepo {
    Iterable<Product> findAll();
    Product save(Product product);
    List<Product> findBySeasonMonthsContaining(Month month);
    Product findByName(String name);
    double sumValidStockByProduct(Long productId);
    Product findById(Long id);
    Long count();
    Double getOrganicProducts();
    List<ProductBatch> findProductsBySupplier(Supplier supplier);
    ProductBatch save(ProductBatch productBatch);
}
