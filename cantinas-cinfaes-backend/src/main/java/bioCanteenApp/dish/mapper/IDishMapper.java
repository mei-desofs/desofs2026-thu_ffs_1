package bioCanteenApp.dish.mapper;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.dto.DishDto;

public interface IDishMapper {
    Dish toDomain(DishDto dto);
    DishDto toDTO(Dish dish);
}
