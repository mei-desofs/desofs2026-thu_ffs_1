package bioCanteenApp.dish.dto;

import bioCanteenApp.ingredients.dto.IngredientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDishDTO {
    private String dishName;
    private List<IngredientDto> ingredients;
    private String nutritionalInformation;
    private List<String> allergens;
}