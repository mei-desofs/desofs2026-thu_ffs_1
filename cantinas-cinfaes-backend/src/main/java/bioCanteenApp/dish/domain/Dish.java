package bioCanteenApp.dish.domain;

import bioCanteenApp.ingredients.domain.Ingredient;
import jakarta.persistence.*;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.recipes.domain.Recipe;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dishName;

    @Column
    private String nutritionalInformation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishType dishType;

    @OneToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishIngredient> dishIngredients = new ArrayList<>();


    public Dish() { }

    public Dish(String dishName, String nutritionalInformation, DishType dishType, Recipe recipe, List<DishIngredient> ingredients) {
        this.dishName = dishName;
        this.nutritionalInformation = nutritionalInformation;
        this.dishType = dishType;
        this.recipe = recipe;
        this.dishIngredients = ingredients;
    }

    public Dish(String dishName, DishType dishType) {
        this.dishName = dishName;
        this.dishType = dishType;
        this.dishIngredients = new ArrayList<>();
    }

    public List<Ingredient> getIngredients() {
        return dishIngredients.stream()
                .map(DishIngredient::getIngredient)
                .toList();
    }

    public void addIngredient(Ingredient ingredient, Double quantity) {
        DishIngredient dishIngredient = new DishIngredient();
        dishIngredient.setDish(this);
        dishIngredient.setIngredient(ingredient);
        dishIngredient.setQuantity(quantity);
        this.dishIngredients.add(dishIngredient);
    }

}