package bioCanteenApp.canteens.repository;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CanteenRepo implements ICanteenRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Canteen save(Canteen canteen) {
        if (canteen.getId() == null) {
            entityManager.persist(canteen);
        } else {
            canteen = entityManager.merge(canteen);
        }
        return canteen;
    }

    @Override
    public Optional<Canteen> findById(Long id) {
        Canteen canteen = entityManager.find(Canteen.class, id);
        return Optional.ofNullable(canteen);
    }

    @Override
    public List<Canteen> findAll() {
        TypedQuery<Canteen> query =
                entityManager.createQuery("SELECT c FROM Canteen c", Canteen.class);
        return query.getResultList();
    }

    @Override
    public Optional<Canteen> findByName(String name) {
        TypedQuery<Canteen> query =
                entityManager.createQuery("SELECT c FROM Canteen c WHERE c.name = :name", Canteen.class);
        query.setParameter("name", name);

        List<Canteen> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Canteen> getAllCanteensByVillage(String village) {
        TypedQuery<Canteen> query = entityManager.createQuery(
                "SELECT c FROM Canteen c WHERE c.location.village = :village",
                Canteen.class
        );
        query.setParameter("village", Enum.valueOf(Village.class, village));
        return query.getResultList();
    }

    @Override
    public List<Canteen> getAllCanteensByMunicipality(String municipality) {
        TypedQuery<Canteen> query = entityManager.createQuery(
                "SELECT c FROM Canteen c WHERE c.location.municipality = :municipality",
                Canteen.class
        );
        query.setParameter("municipality", Enum.valueOf(Municipality.class, municipality));
        return query.getResultList();
    }

}
