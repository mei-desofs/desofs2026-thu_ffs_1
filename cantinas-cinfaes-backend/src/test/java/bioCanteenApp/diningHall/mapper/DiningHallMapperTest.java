package bioCanteenApp.diningHall.mapper;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.mappers.DiningHallMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiningHallMapperTest {

    private final DiningHallMapper mapper = new DiningHallMapper();

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFieldsWithCanteenAndWastes() {
        Canteen canteen = new Canteen("Cantina A", null, 100, true);
        canteen.setId(1L);

        DiningHall diningHall = new DiningHall("Sala 1", canteen);
        diningHall.setId(10L);
        diningHall.setWastes(List.of());

        DiningHallDTO dto = mapper.toDTO(diningHall);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Sala 1", dto.getName());
        assertEquals(1L, dto.getCanteenId());
        assertEquals("Cantina A", dto.getCanteenName());
        assertEquals(0, dto.getWastesCount());
    }

    @Test
    void toDTO_withNullCanteenAndWastes_mapsDefaults() {
        DiningHall diningHall = new DiningHall("Sala 2", null);
        diningHall.setId(20L);
        diningHall.setWastes(null);

        DiningHallDTO dto = mapper.toDTO(diningHall);

        assertNotNull(dto);
        assertEquals("Sala 2", dto.getName());
        assertNull(dto.getCanteenId());
        assertNull(dto.getCanteenName());
        assertEquals(0, dto.getWastesCount());
    }
}
