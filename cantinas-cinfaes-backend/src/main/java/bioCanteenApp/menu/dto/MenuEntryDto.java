package bioCanteenApp.menu.dto;
import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;

import bioCanteenApp.dish.dto.DishDto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntryDto {
    private Long id;
    private String weekDay;
    private String date;
    private List<MenuEntryDishDto> dishes;
}