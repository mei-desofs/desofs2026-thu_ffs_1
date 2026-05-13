package bioCanteenApp.provisioning.repository;

import bioCanteenApp.provisioning.domain.ProvisioningItem;
import bioCanteenApp.provisioning.domain.ProvisioningType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ProvisioningItemRepo implements IProvisioningItemRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProvisioningItem save(ProvisioningItem item) {
        if (item.getId() == null) {
            entityManager.persist(item);
        } else {
            item = entityManager.merge(item);
        }
        return item;
    }

    @Override
    public List<ProvisioningItem> findByMenuAndType(Long menuId, ProvisioningType type) {
        TypedQuery<ProvisioningItem> query =
                entityManager.createQuery(
                        "SELECT p FROM ProvisioningItem p " +
                                "WHERE p.menu.id = :menuId AND p.type = :type",
                        ProvisioningItem.class);

        query.setParameter("menuId", menuId);
        query.setParameter("type", type);

        return query.getResultList();
    }

    @Override
    public void deleteByMenuAndType(Long menuId, ProvisioningType type) {
        entityManager.createQuery(
                        "DELETE FROM ProvisioningItem p WHERE p.menu.id = :menuId AND p.type = :type"
                )
                .setParameter("menuId", menuId)
                .setParameter("type", type)
                .executeUpdate();
    }
}
