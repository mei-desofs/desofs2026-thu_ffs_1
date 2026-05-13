package bioCanteenApp.reservation.mappers;

import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.reservation.domain.Reservation;
import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.users.mapper.IUserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import bioCanteenApp.users.repository.UserRepo;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    private final IUserRepo userRepo;
    private final IMenuRepo menuRepo;

    public ReservationMapper(IMenuRepo dishRepo, IUserRepo userRepo) {
        this.menuRepo = dishRepo;
        this.userRepo = userRepo;
    }

    public ReservationDTO toDTO(Reservation reservation) {
        if (reservation == null) return null;

        return ReservationDTO.builder()
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .menuEntryDishId(reservation.getMenuEntryDish() != null ? reservation.getMenuEntryDish().getId() : null)
                .reservationDateTime(reservation.getReservationDateTime())
                .status(reservation.getStatus())
                .build();
    }

    public Reservation toDomain(ReservationDTO dto) {
        if (dto == null) return null;

        return new Reservation(
                dto.getUserId() != null ? userRepo.findById(dto.getUserId()) : null,
                dto.getMenuEntryDishId() != null ? menuRepo.findMenuEntryDishById(dto.getMenuEntryDishId()) : null,
                dto.getReservationDateTime(),
                dto.getStatus()
        );
    }
}
