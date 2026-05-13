package bioCanteenApp.menu.dto;

import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.users.domain.User;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDto{
    private Long id;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private MenuStatus status;
    private List<MenuEntryDto> entries;
    private User dieticianId;
}