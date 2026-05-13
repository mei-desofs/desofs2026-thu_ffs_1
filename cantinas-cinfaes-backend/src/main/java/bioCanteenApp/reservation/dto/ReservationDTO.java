package bioCanteenApp.reservation.dto;

import bioCanteenApp.reservation.domain.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private Long userId;
    private Long menuEntryDishId;
    private LocalDateTime reservationDateTime;
    private ReservationStatus status;
}
