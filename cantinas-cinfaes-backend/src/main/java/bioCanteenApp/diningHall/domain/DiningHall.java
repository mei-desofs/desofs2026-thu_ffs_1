package bioCanteenApp.diningHall.domain;

import bioCanteenApp.waste.domain.Waste;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import bioCanteenApp.canteens.domain.Canteen;

import java.util.List;

@Entity
@Table(name = "dining_halls")
@Getter
@Setter
public class DiningHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "canteen_id", nullable = false)
    private Canteen canteen;

    @OneToMany(mappedBy = "diningHall")
    private List<Waste> wastes;

    protected DiningHall() {}

    public DiningHall(String name, Canteen canteen) {
        this.name = name;
        this.canteen = canteen;
    }
}
