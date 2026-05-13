package bioCanteenApp.menu.service;

import bioCanteenApp.menu.dto.MenuDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IMenuService {

    List<MenuDto> getAllMenus();

    List<MenuDto> getMenusByWeek(LocalDate startDate, LocalDate endDate);

    MenuDto createMenu(MenuDto dto);

    MenuDto generateMenu(LocalDate startDate, LocalDate endDate);

    void publishMenu(LocalDate start, LocalDate end, Long dietitianId);

    Map<String, Object> getPlanningStats();

    void closeMenu(LocalDate start, LocalDate end);
}
