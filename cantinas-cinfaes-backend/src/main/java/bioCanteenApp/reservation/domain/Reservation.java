package bioCanteenApp.reservation.domain;

import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "menu_entry_dish_id", nullable = false)
    private MenuEntryDish menuEntryDish;

    @Column(nullable = false)
    private LocalDateTime reservationDateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation() { }

    public Reservation(User user, MenuEntryDish menuEntryDish, LocalDateTime reservationDateTime, ReservationStatus status) {
        this.user = user;
        this.menuEntryDish = menuEntryDish;
        this.reservationDateTime = reservationDateTime;
        this.status = status;
    }
}
