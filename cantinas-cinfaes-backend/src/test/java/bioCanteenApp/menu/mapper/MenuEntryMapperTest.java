package bioCanteenApp.menu.mapper;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.mapper.DishMapper;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.dto.MenuEntryDishDto;
import bioCanteenApp.menu.dto.MenuEntryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuEntryMapperTest {

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private MenuEntryMapper mapper;

    @Test
    void toDomain_withNull_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_withNullDishes_mapsFields() {
        MenuEntryDto dto = MenuEntryDto.builder()
                .id(1L)
                .weekDay("MONDAY")
                .date("2026-06-10")
                .dishes(null)
                .build();

        MenuEntry entry = mapper.toDomain(dto);

        assertNotNull(entry);
        assertEquals(1L, entry.getId());
        assertEquals("MONDAY", entry.getWeekDay());
        assertEquals(LocalDate.of(2026, 6, 10), entry.getDate());
        assertTrue(entry.getMenuEntryDishes().isEmpty());
    }

    @Test
    void toDomain_withNullDate_mapsNullDate() {
        MenuEntryDto dto = MenuEntryDto.builder()
                .weekDay("TUESDAY")
                .date(null)
                .dishes(null)
                .build();

        MenuEntry entry = mapper.toDomain(dto);

        assertNull(entry.getDate());
    }

    @Test
    void toDomain_withDishes_mapsDishes() {
        DishDto dishDto = DishDto.builder().id(1L).dishName("Frango").build();
        Dish dish = new Dish("Frango", DishType.MEAT);
        when(dishMapper.toDomain(dishDto)).thenReturn(dish);

        MenuEntryDishDto medDto = MenuEntryDishDto.builder().dish(dishDto).build();
        MenuEntryDto dto = MenuEntryDto.builder()
                .weekDay("WEDNESDAY")
                .date("2026-06-11")
                .dishes(List.of(medDto))
                .build();

        MenuEntry entry = mapper.toDomain(dto);

        assertEquals(1, entry.getMenuEntryDishes().size());
        assertEquals(dish, entry.getMenuEntryDishes().get(0).getDish());
    }

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFields() {
        Dish dish = new Dish("Bacalhau", DishType.FISH);
        dish.setId(2L);
        DishDto dishDto = DishDto.builder().id(2L).dishName("Bacalhau").build();
        when(dishMapper.toDTO(dish)).thenReturn(dishDto);

        MenuEntryDish med = new MenuEntryDish();
        med.setId(10L);
        med.setDish(dish);

        MenuEntry entry = new MenuEntry();
        entry.setId(5L);
        entry.setWeekDay("THURSDAY");
        entry.setDate(LocalDate.of(2026, 6, 12));
        entry.setMenuEntryDishes(List.of(med));

        MenuEntryDto dto = mapper.toDTO(entry);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("THURSDAY", dto.getWeekDay());
        assertEquals("2026-06-12", dto.getDate());
        assertEquals(1, dto.getDishes().size());
        assertEquals(10L, dto.getDishes().get(0).getMenuEntryDishId());
        assertEquals(dishDto, dto.getDishes().get(0).getDish());
    }

    @Test
    void toDTO_withNullDate_mapsNullDate() {
        MenuEntry entry = new MenuEntry();
        entry.setWeekDay("FRIDAY");
        entry.setDate(null);
        entry.setMenuEntryDishes(List.of());

        MenuEntryDto dto = mapper.toDTO(entry);

        assertNull(dto.getDate());
    }
}
