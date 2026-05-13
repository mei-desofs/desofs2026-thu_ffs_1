package bioCanteenApp.waste.mapper;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;

import java.util.List;

public interface IWasteMapper {
    Waste toDomain(WasteDTO dto);
    WasteDTO toDTO(Waste waste);
}
