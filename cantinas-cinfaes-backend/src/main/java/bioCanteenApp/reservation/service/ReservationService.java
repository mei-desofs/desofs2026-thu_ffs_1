// java
package bioCanteenApp.reservation.service;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.reservation.domain.Reservation;
import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.mappers.ReservationMapper;
import bioCanteenApp.reservation.repository.IReservationRepo;
import org.springframework.stereotype.Service;
import static bioCanteenApp.utils.exceptions.ReservationExceptions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService implements IReservationService {

    private final IReservationRepo repo;
    private final IMenuRepo menuRepo;
    private final ReservationMapper mapper;

    public ReservationService(IReservationRepo repo, IMenuRepo menuRepo, ReservationMapper mapper) {
        this.repo = repo;
        this.menuRepo = menuRepo;
        this.mapper = mapper;
    }

    public ReservationDTO createReservation(ReservationDTO dto) {
        Reservation reservation = mapper.toDomain(dto);

        Long userId = dto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to create a reservation");
        }

        List<Reservation> existing = repo.findByUserId(userId);

        // Check same menuEntryDish id (prevent reserving same dish twice)
        Long newMedId = reservation.getMenuEntryDish() != null ? reservation.getMenuEntryDish().getId() : null;
        if (newMedId != null) {
            boolean hasSameMed = existing.stream()
                    .anyMatch(r -> r.getMenuEntryDish() != null && newMedId.equals(r.getMenuEntryDish().getId()));
            if (hasSameMed) {
                throw new ReservationAlreadyExists(userId);
            }
        }

        // Check same menu id (prevent more than one reservation for the same menu/week)
        Long newMenuId;
        if (newMedId != null) {
            // Assumes IMenuRepo has a method like Optional<Menu> findByMenuEntryDishId(Long menuEntryDishId)
            Optional<Menu> menuOpt = menuRepo.findByMenuEntryDishId(newMedId);
            if (menuOpt.isPresent()) {
                newMenuId = menuOpt.get().getId();
            } else {
                newMenuId = null;
            }
        } else {
            newMenuId = null;
        }

        // If we have a menu id, check whether the user already has a reservation for that menu
        if (newMenuId != null) {
            boolean hasSameMenu = existing.stream()
                    .anyMatch(r -> {
                        Long existingMedId = r.getMenuEntryDish() != null ? r.getMenuEntryDish().getId() : null;
                        if (existingMedId == null) return false;
                        Optional<Menu> existingMenuOpt = menuRepo.findByMenuEntryDishId(existingMedId);
                        return existingMenuOpt.map(m -> newMenuId.equals(m.getId())).orElse(false);
                    });
            if (hasSameMenu) {
                throw new ReservationAlreadyExists(userId);
            }
        }

        // Check same date (prevent multiple reservations on the same day)
        // Domain uses LocalDate in bootstrap; adapt if your domain field is different.
        if (reservation.getReservationDateTime() != null) {
            boolean hasSameDate = existing.stream()
                    .anyMatch(r -> r.getReservationDateTime() != null && reservation.getReservationDateTime().equals(r.getReservationDateTime()));
            if (hasSameDate) {
                throw new ReservationAlreadyExists(userId);
            }
        }

        Reservation saved = repo.save(reservation);
        return mapper.toDTO(saved);
    }

    public List<ReservationDTO> getAllReservations() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ReservationDTO getById(Long id) {
        Reservation reservation = repo.findById(id)
                .orElseThrow(() -> new ReservationNotFound(id));
        return mapper.toDTO(reservation);
    }

    public List<ReservationDTO> getByUserId(Long userId) {
        return repo.findByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
