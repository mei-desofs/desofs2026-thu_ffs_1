package bioCanteenApp.menu.mapper;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.mapper.DishMapper;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.dto.MenuEntryDishDto;
import bioCanteenApp.menu.dto.MenuEntryDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuEntryMapper implements IMenuEntryMapper {

    private final DishMapper dishMapper;

    @Override
    public MenuEntry toDomain(MenuEntryDto dto) {
        if (dto == null) {
            return null;
        }

        MenuEntry entry = new MenuEntry();
        entry.setId(dto.getId());
        entry.setWeekDay(dto.getWeekDay());
        entry.setDate(dto.getDate() != null ? LocalDate.parse(dto.getDate()) : null);

        if (dto.getDishes() != null) {
            entry.setMenuEntryDishes(
                    dto.getDishes().stream()
                            .map(dishDto -> new MenuEntryDish(entry, dishMapper.toDomain(dishDto.getDish())))
                            .collect(Collectors.toList())
            );
        }

        return entry;
    }

    @Override
    public MenuEntryDto toDTO(MenuEntry entry) {
        if (entry == null) return null;

        var dishes = entry.getMenuEntryDishes().stream()
                .map(med -> new MenuEntryDishDto(
                        med.getId(),
                        dishMapper.toDTO(med.getDish())
                ))
                .collect(Collectors.toList());

        return new MenuEntryDto(
                entry.getId(),
                entry.getWeekDay(),
                entry.getDate() != null ? entry.getDate().toString() : null,
                dishes
        );
    }
}