package bioCanteenApp.menu.mapper;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.repository.IMenuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuMapper implements IMenuMapper {

    private final IMenuEntryMapper menuEntryMapper;
    private final IMenuRepo menuRepo;

    @Override
    public Menu toDomain(MenuDto dto) {

        Menu menu = menuRepo.findById(dto.getId()).orElse(null);
        if (menu != null) {
            return menu;
        }

        return new Menu(
                dto.getWeekStartDate(),
                dto.getWeekEndDate(),
                dto.getStatus(),
                dto.getEntries().stream()
                        .map(menuEntryMapper::toDomain)
                        .collect(Collectors.toList()),
                dto.getDieticianId()
        );
    }

    @Override
    public MenuDto toDTO(Menu menu) {
        return new MenuDto(
                menu.getId(),
                menu.getWeekStartDate(),
                menu.getWeekEndDate(),
                menu.getStatus(),
                menu.getEntries().stream()
                        .map(menuEntryMapper::toDTO)
                        .collect(Collectors.toList()),
                menu.getDietician()
        );
    }
}
