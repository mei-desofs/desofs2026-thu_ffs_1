package bioCanteenApp.diningHall.repository;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class DiningHallRepository implements IDiningHallRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DiningHall save(DiningHall diningHall) {
        if (diningHall.getId() == null) {
            entityManager.persist(diningHall);
        } else {
            diningHall = entityManager.merge(diningHall);
        }
        return diningHall;
    }

    @Override
    public List<DiningHall> findAll() {
        return entityManager.createQuery("SELECT d FROM DiningHall d", DiningHall.class)
                .getResultList();
    }

    @Override
    public Optional<DiningHall> findByName(String name) {
        List<DiningHall> result = entityManager.createQuery(
                        "SELECT d FROM DiningHall d WHERE d.name = :name", DiningHall.class)
                .setParameter("name", name)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
