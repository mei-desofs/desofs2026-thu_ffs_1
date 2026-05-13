package bioCanteenApp.recipes.repository;

import bioCanteenApp.recipes.domain.Recipe;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class RecipeRepo implements IRecipeRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Recipe save(Recipe recipe) {
        if (recipe.getId() == null) {
            entityManager.persist(recipe);
        } else {
            recipe = entityManager.merge(recipe);
        }
        return recipe;
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        Recipe recipe = entityManager.find(Recipe.class, id);
        return Optional.ofNullable(recipe);
    }

    @Override
    public List<Recipe> findAll() {
        TypedQuery<Recipe> query = entityManager.createQuery(
                "SELECT r FROM Recipe r", Recipe.class
        );
        return query.getResultList();
    }

    @Override
    public Optional<Recipe> findByName(String name) {
        TypedQuery<Recipe> query = entityManager.createQuery(
                "SELECT r FROM Recipe r WHERE r.name = :name",
                Recipe.class
        );
        query.setParameter("name", name);

        List<Recipe> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Recipe r", Long.class);
        return query.getSingleResult();
    }
}
