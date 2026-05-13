package bioCanteenApp.diningHall.service;

import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;

import java.util.List;

public interface IDiningHallService {
    List<DiningHallDTO> getAllDiningHall();
}
