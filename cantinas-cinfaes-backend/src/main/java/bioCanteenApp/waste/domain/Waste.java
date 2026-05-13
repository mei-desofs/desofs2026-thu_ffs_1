package bioCanteenApp.waste.domain;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.suppliers.domain.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "waste")
@Getter
@Setter
public class Waste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    //desperdicio de refeições nao servidas
    private double totalMealsReserved;
    private double mealsNotServed;

    //desperdicio de refeições servidas
    private double servedWaste;
    private double servedTotal;

    @ManyToOne
    @JoinColumn(name = "canteen_id")
    private Canteen canteen;

    @ManyToOne
    @JoinColumn(name = "dining_hall_id")
    private DiningHall diningHall;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public Waste(double totalMealsReserved, double mealsNotServed, double servedWaste, double servedTotal) {
        this.totalMealsReserved = totalMealsReserved;
        this.mealsNotServed = mealsNotServed;
        this.servedWaste = servedWaste;
        this.servedTotal = servedTotal;
    }

    public Waste(LocalDate date,  double totalMealsReserved, double mealsNotServed, double servedWaste, double servedTotal,
                 Canteen canteen, DiningHall diningHall, Supplier supplier) {
        this.date = date;
        this.totalMealsReserved = totalMealsReserved;
        this.mealsNotServed = mealsNotServed;
        this.servedWaste = servedWaste;
        this.servedTotal = servedTotal;
        this.canteen = canteen;
        this.diningHall = diningHall;
        this.supplier = supplier;
    }



    protected Waste() {}
}
