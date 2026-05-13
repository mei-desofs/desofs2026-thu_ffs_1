package bioCanteenApp.menu.domain;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_entries")
@Getter
@Setter
public class MenuEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String weekDay;

    @Column(nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "menuEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuEntryDish> menuEntryDishes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    public MenuEntry(Long id, String weekDay, LocalDate date, List<MenuEntryDish> dishes, Menu menu) {
        this.id = id;
        this.weekDay = weekDay;
        this.date = date;
        this.menuEntryDishes = dishes;
        this.menu = menu;
    }

    public MenuEntry() {
    }

}
