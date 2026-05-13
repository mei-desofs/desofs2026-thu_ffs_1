package bioCanteenApp.recipes.repository;

import bioCanteenApp.recipes.domain.Recipe;

import java.util.List;
import java.util.Optional;

public interface IRecipeRepo {
    Recipe save(Recipe recipe);
    Optional<Recipe> findById(Long id);
    List<Recipe> findAll();
    Optional<Recipe> findByName(String name);

    Long count();
}
