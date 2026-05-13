package bioCanteenApp.products.repository;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.suppliers.domain.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;

@Repository
public class ProductRepo implements IProductRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Iterable<Product> findAll() {
        TypedQuery<Product> query = entityManager.createQuery("SELECT p FROM Product p", Product.class);
        return query.getResultList();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        } else {
            return entityManager.merge(product);
        }
    }

    @Override
    public List<Product> findBySeasonMonthsContaining(Month month) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p JOIN p.seasons s WHERE s = :season", Product.class);

        Season currentSeason = Season.fromMonth(month);
        query.setParameter("season", currentSeason);

        return query.getResultList();
    }

    @Override
    public Product findByName(String name) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.name = :name",
                Product.class
        );
        query.setParameter("name", name);

        List<Product> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p", Long.class);
        return query.getSingleResult();
    }
    @Override
    public Product findById(Long id) {
        return entityManager.find(Product.class, id);
    }
    /**
     * Soma o stock válido (não expirado) de um produto
     */
    @Override
    public double sumValidStockByProduct(Long productId) {
        TypedQuery<Double> query = entityManager.createQuery(
                "SELECT COALESCE(SUM(pb.quantity), 0) " +
                        "FROM ProductBatch pb " +
                        "WHERE pb.product.id = :productId " +
                        "AND pb.isQuarantined = false " +
                        "AND pb.expirationDate >= CURRENT_DATE",
                Double.class
        );

        query.setParameter("productId", productId);
        return query.getSingleResult();
    }

    @Override
    public Double getOrganicProducts() {
        TypedQuery<Double> query = entityManager.createQuery(
        "SELECT (CAST(SUM(CASE WHEN pb.isBio = true THEN 1 ELSE 0 END) AS double) / COUNT(pb)) * 100 " +
        "FROM ProductBatch pb", Double.class
        );

        Double percentage = query.getSingleResult();

        if (percentage == null) {
            return 0.0;
        }

        return Math.round(percentage * 100.0) / 100.0;
    }

    @Override
    public List<ProductBatch> findProductsBySupplier(Supplier supplier) {
        TypedQuery<ProductBatch> query = entityManager.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.supplier = :supplier",
                ProductBatch.class
        );
        query.setParameter("supplier", supplier);
        return query.getResultList();
    }

    @Override
    public ProductBatch save(ProductBatch productBatch) {
        if (productBatch.getId() == null) {
            entityManager.persist(productBatch);
            return productBatch;
        } else {
            return entityManager.merge(productBatch);
        }
    }

}
