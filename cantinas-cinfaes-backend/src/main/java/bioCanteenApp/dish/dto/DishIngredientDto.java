package bioCanteenApp.dish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishIngredientDto {
    private Long ingredientId;
    private Long productId;
    private String ingredientName;
    private Double quantity;
    private String unit;
}
