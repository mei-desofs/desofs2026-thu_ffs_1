package bioCanteenApp.menu.dto;

import bioCanteenApp.dish.dto.DishDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntryDishDto {
    private Long menuEntryDishId;
    private DishDto dish;
}
