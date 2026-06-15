package bioCanteenApp.reservation.repository;

import bioCanteenApp.reservation.domain.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IReservationRepo {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);
    Reservation save(Reservation reservation);
    long countConfirmedByMenuEntryDish(Long menuEntryDishId);
    long averageReservationsForDish(Long dishId,Long menuId);
    long countBackMenusReservations(Long dishId,Long menuId);
    long countConfirmedByDishBetweenDates(Long dishId, LocalDateTime startDate, LocalDateTime endDate);
}
