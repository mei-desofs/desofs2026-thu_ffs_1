package bioCanteenApp.recipes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipes")
@Getter
@Setter
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String instructions;

    public Recipe(String name, String instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    public Recipe (Long id) {
        this.id = id;
    }

    protected Recipe() { }
}
