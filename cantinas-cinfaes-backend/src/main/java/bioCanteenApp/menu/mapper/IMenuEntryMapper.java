package bioCanteenApp.menu.mapper;

import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.dto.MenuEntryDto;

public interface IMenuEntryMapper {
    MenuEntry toDomain(MenuEntryDto dto);
    MenuEntryDto toDTO(MenuEntry entry);
}
