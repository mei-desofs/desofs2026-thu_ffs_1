package bioCanteenApp.dish.service;

import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.GetDishDTO;

import java.util.List;

public interface IDishService {
    GetDishDTO generateDishInformation(GetDishDTO dto);

    List<DishDto> getAll();

    List<DishDto> getDishesWithSeasonalIngredients(List<String> seasonalProductNames);

    List<DishDto> getAlternatives(Long menuEntryDishId);

    void replaceDish(Long menuEntryDishId, Long newDishId);

    List<DishType> getDishType();

    DishDto createDish(DishDto dto);

    Double getOrganicProducts();
}
