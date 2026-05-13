package bioCanteenApp.canteens.dto;

import bioCanteenApp.suppliers.dto.AddressDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanteenDTO {
    private String name;
    private AddressDTO location;
    private Integer capacity;
    private Boolean isQuarantine;
    private Boolean canCookDishes;
}
