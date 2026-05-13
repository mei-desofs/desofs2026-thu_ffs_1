package bioCanteenApp.canteens.mappers;

import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.dto.CanteenDTO;

public interface ICanteenMapper {

    CanteenDTO toDTO(Canteen canteen);

    Canteen toDomain(CanteenDTO dto);
}
