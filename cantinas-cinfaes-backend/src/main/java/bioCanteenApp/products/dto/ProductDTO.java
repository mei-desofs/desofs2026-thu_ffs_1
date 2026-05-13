package bioCanteenApp.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private long id;
    private String name;
    private String unit;
    private Integer expirationDays;
    private List<String> seasonalSeasons;
    private List<String> allergens;
}
