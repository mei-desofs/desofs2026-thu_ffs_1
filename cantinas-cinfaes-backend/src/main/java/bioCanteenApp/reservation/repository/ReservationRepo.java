package bioCanteenApp.reservation.repository;

import bioCanteenApp.reservation.domain.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ReservationRepo implements IReservationRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            entityManager.persist(reservation);
        } else {
            reservation = entityManager.merge(reservation);
        }
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        return Optional.ofNullable(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r",
                Reservation.class
        );
        return query.getResultList();
    }

    @Override
    public List<Reservation> findByUserId(Long userId) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.user.id = :userId",
                Reservation.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public long countConfirmedByMenuEntryDish(Long menuEntryDishId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Reservation r " +
                        "WHERE r.menuEntryDish.id = :id AND r.status = 'CONFIRMED'",
                Long.class
        );
        query.setParameter("id", menuEntryDishId);
        return query.getSingleResult();
    }

    @Override
    public long averageReservationsForDish(Long dishId, Long menuId) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(r) FROM Reservation r " +
                                "WHERE r.menuEntryDish.dish.id = :dishId " +
                                "AND r.status = 'CONFIRMED' AND r.menuEntryDish.menuEntry.menu.id != :menuId",
                        Long.class
                ).setParameter("dishId", dishId).setParameter("menuId", menuId)
                .getSingleResult();

        Long menus = entityManager.createQuery(
                        "SELECT COUNT(DISTINCT me.menu.id) " +
                                "FROM MenuEntryDish med JOIN med.menuEntry me " +
                                "WHERE med.dish.id = :dishId AND me.menu.id != :menuId",
                        Long.class
                ).setParameter("dishId", dishId).setParameter("menuId", menuId)
                .getSingleResult();

        return menus == 0 ? 0 : total / menus;
    }

    @Override
    public long countBackMenusReservations(Long dishId, Long menuId){
        Long menus = entityManager.createQuery(
                        "SELECT COUNT(DISTINCT me.menu.id) " +
                                "FROM MenuEntryDish med JOIN med.menuEntry me " +
                                "WHERE med.dish.id = :dishId AND me.menu.id != :menuId",
                        Long.class
                ).setParameter("dishId", dishId).setParameter("menuId", menuId)
                .getSingleResult();
        return menus;
    }

}
