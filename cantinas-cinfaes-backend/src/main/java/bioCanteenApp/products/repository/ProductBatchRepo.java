package bioCanteenApp.products.repository;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductBatchRepo implements IProductBatchRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Iterable<ProductBatch> findAll() {
        TypedQuery<ProductBatch> query =
                entityManager.createQuery(
                        "SELECT pb FROM ProductBatch pb",
                        ProductBatch.class
                );
        return query.getResultList();
    }

    @Override
    public ProductBatch findById(Long id) {
        return entityManager.find(ProductBatch.class, id);
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

    /**
     * Todos os batches de um produto
     */
    @Override
    public List<ProductBatch> findByProduct(Product product) {
        TypedQuery<ProductBatch> query =
                entityManager.createQuery(
                        "SELECT pb FROM ProductBatch pb " +
                                "WHERE pb.product = :product " +
                                "ORDER BY pb.expirationDate ASC",
                        ProductBatch.class
                );

        query.setParameter("product", product);
        return query.getResultList();
    }

    /**
     * Batches válidos (não expirados) de um produto
     */
    @Override
    public List<ProductBatch> findValidBatchesByProduct(Product product) {
        TypedQuery<ProductBatch> query =
                entityManager.createQuery(
                        "SELECT pb FROM ProductBatch pb " +
                                "WHERE pb.product = :product " +
                                "AND pb.expirationDate >= CURRENT_DATE " +
                                "ORDER BY pb.expirationDate ASC",
                        ProductBatch.class
                );

        query.setParameter("product", product);
        return query.getResultList();
    }

    /**
     * Soma do stock válido (não expirado) de um produto
     */
    @Override
    public double sumValidStockByProduct(Long productId) {
        TypedQuery<Double> query =
                entityManager.createQuery(
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

    /**
     * Remove um batch
     */
    @Override
    public void delete(ProductBatch productBatch) {
        entityManager.remove(
                entityManager.contains(productBatch)
                        ? productBatch
                        : entityManager.merge(productBatch)
        );
    }

    /**
     * Batches expirados (útil para limpeza ou alertas)
     */
    @Override
    public List<ProductBatch> findExpiredBatches() {
        TypedQuery<ProductBatch> query =
                entityManager.createQuery(
                        "SELECT pb FROM ProductBatch pb " +
                                "WHERE pb.expirationDate < CURRENT_DATE",
                        ProductBatch.class
                );

        return query.getResultList();
    }
}
