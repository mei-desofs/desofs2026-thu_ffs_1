package bioCanteenApp.users.dto;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String password;
    private String role;
    private Long canteenId;
    private Long diningHallId;
}
