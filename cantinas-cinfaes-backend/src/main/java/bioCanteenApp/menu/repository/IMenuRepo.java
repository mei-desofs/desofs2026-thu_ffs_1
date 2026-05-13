package bioCanteenApp.menu.repository;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntryDish;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IMenuRepo {
    Iterable<Menu> findAll();
    Menu save(Menu menu);
    Optional<Menu> findById(Long id);
    Optional<Menu> findByMenuEntryDishId(Long menuEntryDishId);
    MenuEntryDish findMenuEntryDishById(Long id);

    List<Menu> findByMenuDates(LocalDate startDate, LocalDate endDate, LocalDate startDate1, LocalDate endDate1);
}
