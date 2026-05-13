package bioCanteenApp.menu.domain;

import bioCanteenApp.dish.domain.Dish;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "menu_entry_dishes")
@Getter
@Setter
public class MenuEntryDish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_entry_id", nullable = false)
    private MenuEntry menuEntry;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    protected MenuEntryDish() {}

    public MenuEntryDish(MenuEntry entry, Dish dish) {
        this.menuEntry = entry;
        this.dish = dish;
    }
}
