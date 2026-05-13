package bioCanteenApp.diningHall.mappers;

import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;

public interface IDiningHallMapper {
    DiningHallDTO toDTO(DiningHall diningHall);
}
