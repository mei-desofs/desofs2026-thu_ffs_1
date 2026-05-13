package bioCanteenApp.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserDTO {
    private Long id;
    private String email;
    private String name;
    private String role;
    private Long canteenId;
    private Long diningHallId;
}
