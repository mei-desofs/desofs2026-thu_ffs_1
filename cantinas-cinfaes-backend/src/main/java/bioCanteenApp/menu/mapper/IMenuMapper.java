package bioCanteenApp.menu.mapper;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.dto.MenuDto;

public interface IMenuMapper {

    Menu toDomain(MenuDto dto);

    MenuDto toDTO(Menu menu);
}