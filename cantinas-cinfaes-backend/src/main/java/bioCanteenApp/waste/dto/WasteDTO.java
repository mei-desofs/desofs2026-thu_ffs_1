package bioCanteenApp.waste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteDTO {
    private double totalMealsReserved;
    private double notServedWaste;
    private double servedWaste;
    private double totalMealsConsumed;
}
