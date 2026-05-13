package bioCanteenApp.users.domain;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.waste.domain.Waste;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLSession;
import java.util.List;

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

    @Column
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
}
