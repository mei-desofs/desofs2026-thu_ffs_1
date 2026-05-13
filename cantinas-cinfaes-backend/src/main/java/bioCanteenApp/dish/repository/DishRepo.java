package bioCanteenApp.dish.repository;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.products.domain.Season;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class DishRepo implements IDishRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Dish save(Dish dish) {
        if (dish.getId() == null) {
            entityManager.persist(dish);
        } else {
            dish = entityManager.merge(dish);
        }
        return dish;
    }

    @Override
    public Optional<Dish> findById(Long id) {
        Dish dish = entityManager.find(Dish.class, id);
        return Optional.ofNullable(dish);
    }

    @Override
    public List<Dish> findAll() {
        TypedQuery<Dish> query = entityManager.createQuery(
                "SELECT d FROM Dish d", Dish.class
        );
        return query.getResultList();
    }

    @Override
    public Optional<Dish> findByName(String name) {
        TypedQuery<Dish> query = entityManager.createQuery(
                "SELECT d FROM Dish d WHERE d.dishName = :name",
                Dish.class
        );
        query.setParameter("name", name);

        List<Dish> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Dish> findSeasonalAlternatives(
            DishType dishType,
            Season season,
            Long excludeDishId
    ) {
        return entityManager.createQuery(
                        """
                        SELECT DISTINCT d
                        FROM Dish d
                        JOIN d.dishIngredients i
                        JOIN i.ingredient ing
                        JOIN ing.product p
                        JOIN p.seasons s
                        WHERE d.dishType = :dishType
                          AND d.id <> :excludeDishId
                          AND s = :season
                        """,
                        Dish.class
                )
                .setParameter("dishType", dishType)
                .setParameter("excludeDishId", excludeDishId)
                .setParameter("season", season)
                .getResultList();
    }

    @Override
    public List<Dish> findAllByType(DishType dishType) {
        TypedQuery<Dish> query = entityManager.createQuery(
                "SELECT d FROM Dish d WHERE d.dishType = :dishType",
                Dish.class
        );
        query.setParameter("dishType", dishType);
        return query.getResultList();
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(d) FROM Dish d", Long.class
        );
        return query.getSingleResult();
    }

}
