package bioCanteenApp.users.domain;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String name;

    @Column(nullable = false)
    private String password;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "canteen_id")
    private Canteen canteen;

    @ManyToOne
    @JoinColumn(name = "dining_hall_id")
    private DiningHall diningHall;

    // password changed after six months
    @Column
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private int failedAttempts = 0;

    @Column
    private LocalDateTime lockedUntil;

    @Column
    private String lastDeviceId;

    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = Role.USER;
    }

    public User(String email, String name, String password, Role role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // User associated with a canteen
    public User(String email, String name, String password, Role role, Canteen canteen) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.canteen = canteen;
    }

    // User associated with a dining hall
    public User(String email, String name, String password, Role role, DiningHall diningHall) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.diningHall = diningHall;
    }

    // User associated with both canteen and dining hall
    public User(String email, String name, String password, Role role, Canteen canteen, DiningHall diningHall) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.canteen = canteen;
        this.diningHall = diningHall;
    }

    protected User() { }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public long secondsUntilUnlock() {
        if (!isLocked()) return 0;
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), lockedUntil);
    }
}
