package bioCanteenApp.waste.service;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.mapper.IWasteMapper;
import bioCanteenApp.waste.repository.IWasteRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WasteService implements IWasteService {

    private final IWasteRepo wasteRepo;
    private final IWasteMapper wasteMapper;
    private final EntityManager entityManager;

    @Override
    public WasteDTO getDailyWaste() {
        Waste waste = wasteRepo.findByDate(LocalDate.now());
        return wasteMapper.toDTO(waste);
    }

    @Override
    public WasteDTO getWeeklyWaste() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        Waste waste = wasteRepo.findByDateBetween(start, end);
        return wasteMapper.toDTO(waste);
    }

    @Override
    public WasteDTO getMonthlyWaste() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1);

        Waste waste = wasteRepo.findByDateBetween(start, end);
        return wasteMapper.toDTO(waste);
    }

    @Override
    public WasteDTO getAllWaste() {
        Waste waste = wasteRepo.findAggregateAll();
        return wasteMapper.toDTO(waste);
    }

    @Override
    public WasteDTO aggregateWaste(Long canteenId, Long diningHallId, Long supplierId, LocalDate start, LocalDate end) {
        return wasteRepo.aggregateWaste(canteenId, diningHallId, supplierId, start, end);
    }

    @Override
    public LocalDate[] getDateRange(String period) {
        return wasteRepo.getDateRange(period);
    }
}
