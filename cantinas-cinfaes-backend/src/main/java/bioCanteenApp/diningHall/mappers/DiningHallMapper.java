package bioCanteenApp.diningHall.mappers;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;
import org.springframework.stereotype.Component;

@Component
public class DiningHallMapper implements IDiningHallMapper {

    @Override
    public DiningHallDTO toDTO(DiningHall diningHall) {
        if (diningHall == null) return null;

        return DiningHallDTO.builder()
                .id(diningHall.getId())
                .name(diningHall.getName())
                .canteenId(diningHall.getCanteen() != null ? diningHall.getCanteen().getId() : null)
                .canteenName(diningHall.getCanteen() != null ? diningHall.getCanteen().getName() : null)
                .wastesCount(diningHall.getWastes() != null ? diningHall.getWastes().size() : 0)
                .build();
    }
}
