package bioCanteenApp.menu.repository;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.domain.MenuStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class MenuRepo implements IMenuRepo{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Iterable<Menu> findAll() {
        TypedQuery<Menu> query = entityManager.createQuery("SELECT m FROM Menu m", Menu.class);

        return query.getResultList();
    }

    @Override
    public Menu save(Menu menu) {
        if (menu.getId() == null) {
            entityManager.persist(menu);
        } else {
            return entityManager.merge(menu);
        }

        return menu;
    }

    @Override
    public Optional<Menu> findById(Long id) {
        Menu menu = entityManager.find(Menu.class, id);
        return menu != null ? Optional.of(menu) : Optional.empty();
    }

    @Override
    public MenuEntryDish findMenuEntryDishById(Long id) {
        return entityManager.find(MenuEntryDish.class, id);
    }

    @Override
    public Optional<Menu> findByMenuEntryDishId(Long menuEntryDishId) {
        TypedQuery<Menu> query = entityManager.createQuery(
                "SELECT m FROM Menu m JOIN m.entries me JOIN me.menuEntryDishes med WHERE med.id = :medId",
                Menu.class);
        query.setParameter("medId", menuEntryDishId);
        List<Menu> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Menu> findByMenuDates(LocalDate startDate, LocalDate endDate, LocalDate startDate1, LocalDate endDate1) {
        TypedQuery<Menu> query = entityManager.createQuery(
                "SELECT m FROM Menu m WHERE " +
                        "(m.weekStartDate BETWEEN :startDate AND :endDate) " +
                        "OR (m.weekEndDate BETWEEN :startDate1 AND :endDate1)",
                Menu.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("startDate1", startDate1);
        query.setParameter("endDate1", endDate1);

        return query.getResultList();
    }


    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM Menu m", Long.class);
        return query.getSingleResult();
    }

    public long countByStatus(MenuStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM Menu m WHERE m.status = :status", Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
}
