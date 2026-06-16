package bioCanteenApp.waste.mapper;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WasteMapper implements IWasteMapper {

    @Override
    public Waste toDomain(WasteDTO dto) {
        if (dto == null) return null;

        return new Waste(
                dto.getTotalMealsReserved(),
                dto.getNotServedWaste(),
                dto.getServedWaste(),
                dto.getTotalMealsConsumed()
        );
    }

    @Override
    public WasteDTO toDTO(Waste waste) {
        if (waste == null) return null;

        return new WasteDTO(
            waste.getTotalMealsReserved(),
            waste.getMealsNotServed(),
            waste.getServedWaste(),
            waste.getServedTotal()
        );
    }

}
