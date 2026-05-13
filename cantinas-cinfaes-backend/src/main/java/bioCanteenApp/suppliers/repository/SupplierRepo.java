package bioCanteenApp.suppliers.repository;

import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.suppliers.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SupplierRepo implements ISupplierRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Supplier save(Supplier supplier) {
        if (supplier.getId() == null) {
            entityManager.persist(supplier);
            return supplier;
        } else {
            return entityManager.merge(supplier);
        }    }

    @Override
    public SupplierApplication save(SupplierApplication supplierApplication) {
        if (supplierApplication.getId() == null) {
            entityManager.persist(supplierApplication);
            return supplierApplication;
        } else {
            return entityManager.merge(supplierApplication);
        }    }

    @Override
    public Optional<SupplierApplication> findByEmail(String email) {
        TypedQuery<SupplierApplication> query = entityManager.createQuery(
                "SELECT sa FROM SupplierApplication sa WHERE sa.email = :email", SupplierApplication.class);
        query.setParameter("email", email);

        try {
            return Optional.of(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public long countTotalSuppliers() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(s) FROM Supplier s", Long.class);
        return query.getSingleResult();
    }

    @Override
    public long countApplicationsByStatus(SupplierApplicationStatus supplierApplicationStatus) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(sa) FROM SupplierApplication sa WHERE sa.status = :status", Long.class);
        query.setParameter("status", supplierApplicationStatus);
        return query.getSingleResult();
    }

    @Override
    public List<Supplier> findAll() {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s",
                Supplier.class
        );
        return query.getResultList();
    }

    @Override
    public Supplier findBySupplierEmail(String email) {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s WHERE s.user.email = :email", Supplier.class);
        query.setParameter("email", email);

        try {
            return query.getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Supplier> findAllSuppliersByOrderByProduct(Product product) {
        String jpql = "SELECT DISTINCT s FROM Supplier s " +
                "JOIN s.applicationId sa " +
                "JOIN sa.supplierCapacity sc " +
                "WHERE sc.productName = :productName " +
                "ORDER BY sa.applicationDate ASC";

        TypedQuery<Supplier> query = entityManager.createQuery(jpql, Supplier.class);
        query.setParameter("productName", product.getName());

        return query.getResultList();
    }

    @Override
    public List<SupplierApplication> findAllApplications() {
        TypedQuery<SupplierApplication> query = entityManager.createQuery(
                "SELECT sa FROM SupplierApplication sa",
                SupplierApplication.class
        );
        return query.getResultList();
    }

    @Override
    public List<Supplier> getAllSuppliersByVillage(String village) {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s WHERE s.address.village = :village",
                Supplier.class
        );
        query.setParameter("village",  Enum.valueOf(Village.class, village));
        return query.getResultList();
    }

    @Override
    public List<Supplier> findSuppliersByName(String name) {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s WHERE LOWER(s.user.name) LIKE LOWER(:name)", Supplier.class
        );
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    @Override
    public List<Supplier> findSuppliersByVillage(String village) {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s WHERE s.address.village = :village", Supplier.class
        );
        query.setParameter("village", Enum.valueOf(Village.class, village));
        return query.getResultList();
    }

    @Override
    public List<Supplier> findSuppliersByMunicipality(String municipality) {
        TypedQuery<Supplier> query = entityManager.createQuery(
                "SELECT s FROM Supplier s WHERE s.address.municipality = :municipality", Supplier.class
        );
        query.setParameter("municipality", Enum.valueOf(Municipality.class, municipality));
        return query.getResultList();
    }

    @Override
    public Supplier findById(Long id) {
        return entityManager.find(Supplier.class, id);
    }

    @Override
    public List<SupplierApplication> findApprovedApplicationsFromNonQuarantinedSuppliers() {
        return entityManager.createQuery("""
        SELECT sa
        FROM SupplierApplication sa
        JOIN Supplier s ON s.applicationId = sa
        WHERE sa.status = :status
          AND s.isQuarantined = false
        ORDER BY sa.applicationDate
    """, SupplierApplication.class)
                .setParameter("status", SupplierApplicationStatus.APPROVED)
                .getResultList();
    }


}
