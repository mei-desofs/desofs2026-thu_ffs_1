package bioCanteenApp.diningHall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningHallDTO {
    private Long id;
    private String name;
    private Long canteenId;
    private String canteenName;
    private int wastesCount;
}
