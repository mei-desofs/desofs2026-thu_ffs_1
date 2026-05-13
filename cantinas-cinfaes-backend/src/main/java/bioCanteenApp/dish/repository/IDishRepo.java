package bioCanteenApp.dish.repository;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.products.domain.Season;

import java.util.List;
import java.util.Optional;

public interface IDishRepo {
    Dish save(Dish dish);
    Optional<Dish> findById(Long id);
    List<Dish> findAll();
    Optional<Dish> findByName(String name);
    List<Dish> findSeasonalAlternatives(DishType dishType, Season season, Long id);
    List<Dish> findAllByType(DishType dishType);
}
