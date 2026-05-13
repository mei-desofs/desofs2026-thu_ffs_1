package bioCanteenApp.menu.domain;

import bioCanteenApp.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
@Getter
@Setter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Column(nullable = false)
    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status = MenuStatus.GENERATED;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuEntry> entries = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "dietician_id")
    private User dietician;

    protected Menu(){

    }
    public Menu(LocalDate weekStartDate, LocalDate weekEndDate, MenuStatus status, List<MenuEntry> entries, User dietician) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.status = status;
        this.entries =  entries;
        this.dietician = dietician;
    }

    public Menu(LocalDate weekStartDate, LocalDate weekEndDate, MenuStatus status) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.status = status;
        this.entries = new ArrayList<>();
    }

}
