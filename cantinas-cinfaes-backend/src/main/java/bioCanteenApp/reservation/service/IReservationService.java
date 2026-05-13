package bioCanteenApp.reservation.service;

import bioCanteenApp.reservation.dto.ReservationDTO;

import java.util.List;

public interface IReservationService {
    ReservationDTO createReservation(ReservationDTO dto);
    List<ReservationDTO> getAllReservations();
    ReservationDTO getById(Long id);
    List<ReservationDTO> getByUserId(Long userId);
}
