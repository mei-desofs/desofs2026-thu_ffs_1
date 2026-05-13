package bioCanteenApp.ingredients.repository;

import bioCanteenApp.ingredients.domain.Ingredient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class IngredientRepo implements IIngredientRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Ingredient save(Ingredient ingredient) {
        if (ingredient.getId() == null) {
            entityManager.persist(ingredient);
        } else {
            ingredient = entityManager.merge(ingredient);
        }
        return ingredient;
    }

    @Override
    public Optional<Ingredient> findById(Long id) {
        Ingredient ingredient = entityManager.find(Ingredient.class, id);
        return Optional.ofNullable(ingredient);
    }

    @Override
    public List<Ingredient> findAll() {
        TypedQuery<Ingredient> query = entityManager.createQuery(
                "SELECT i FROM Ingredient i", Ingredient.class
        );
        return query.getResultList();
    }

    @Override
    public List<Ingredient> findByDish(Long dishId) {
        TypedQuery<Ingredient> query = entityManager.createQuery(
                "SELECT i FROM Ingredient i WHERE i.dish.id = :dishId",
                Ingredient.class
        );
        query.setParameter("dishId", dishId);
        return query.getResultList();
    }

    @Override
    public List<Ingredient> findByName(String name) {
        TypedQuery<Ingredient> query = entityManager.createQuery(
                "SELECT i FROM Ingredient i WHERE LOWER(i.name) = LOWER(:name)",
                Ingredient.class
        );
        query.setParameter("name", name);
        return query.getResultList();
    }

    @Override
    public Long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(i) FROM Ingredient i", Long.class);
        return query.getSingleResult();
    }
}
