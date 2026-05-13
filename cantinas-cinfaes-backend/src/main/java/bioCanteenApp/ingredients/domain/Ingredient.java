package bioCanteenApp.ingredients.domain;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.products.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(nullable = false)
    private Double quantity;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

//    @ManyToOne
//    @JoinColumn(name = "dish_id", nullable = false)
//    private Dish dish;

    protected Ingredient() {}

    public Ingredient(String name, Double quantity, Product product) {
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }
}
