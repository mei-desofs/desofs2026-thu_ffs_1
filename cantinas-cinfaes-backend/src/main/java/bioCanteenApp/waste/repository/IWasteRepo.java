package bioCanteenApp.waste.repository;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;

import java.time.LocalDate;
import java.util.List;

public interface IWasteRepo {
    Waste findByDate(LocalDate date);
    Waste findByDateBetween(LocalDate start, LocalDate end);
    Waste findAggregateAll();
    WasteDTO aggregateWaste(Long canteenId, Long diningHallId, Long supplierId, LocalDate start, LocalDate end);
    LocalDate[] getDateRange(String period);
    Waste save(Waste waste);
    Iterable<Waste> findAll();
}
