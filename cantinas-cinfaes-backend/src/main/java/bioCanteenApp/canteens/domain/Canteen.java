package bioCanteenApp.canteens.domain;

import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.address.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "canteens")
@Getter
@Setter
public class Canteen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private Address location;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean canCookDishes;

    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    private List<DiningHall> diningHalls;

    @Column(nullable = false)
    private Boolean isQuarantined=false;

    public Canteen(String name, Address location, Integer capacity, Boolean canCookDishes) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.canCookDishes = canCookDishes;
    }

    protected Canteen() { }
}
