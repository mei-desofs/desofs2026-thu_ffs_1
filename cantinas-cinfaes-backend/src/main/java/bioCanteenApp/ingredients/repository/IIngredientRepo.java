package bioCanteenApp.ingredients.repository;

import bioCanteenApp.ingredients.domain.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IIngredientRepo {
    Ingredient save(Ingredient ingredient);
    Optional<Ingredient> findById(Long id);
    List<Ingredient> findAll();
    List<Ingredient> findByDish(Long dishId);
    List<Ingredient> findByName(String name);

    Long count();
}
