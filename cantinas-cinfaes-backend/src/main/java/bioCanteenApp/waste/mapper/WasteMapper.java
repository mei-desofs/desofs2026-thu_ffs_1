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
        return null;
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
