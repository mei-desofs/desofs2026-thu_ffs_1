package bioCanteenApp.waste.mapper;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WasteMapperTest {

    private final WasteMapper mapper = new WasteMapper();

    @Test
    void toDomain_alwaysReturnsNull() {
        assertNull(mapper.toDomain(new WasteDTO()));
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFields() {
        Waste waste = new Waste(10.0, 2.0, 1.5, 8.5);

        WasteDTO dto = mapper.toDTO(waste);

        assertNotNull(dto);
        assertEquals(10.0, dto.getTotalMealsReserved());
        assertEquals(2.0, dto.getNotServedWaste());
        assertEquals(1.5, dto.getServedWaste());
        assertEquals(8.5, dto.getTotalMealsConsumed());
    }
}
