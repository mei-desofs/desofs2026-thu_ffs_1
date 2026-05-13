package bioCanteenApp.waste.service;

import bioCanteenApp.waste.dto.WasteDTO;

import java.time.LocalDate;

public interface IWasteService {
    WasteDTO getDailyWaste();
    WasteDTO getWeeklyWaste();
    WasteDTO getMonthlyWaste();
    WasteDTO getAllWaste();
    LocalDate[] getDateRange(String period);
    WasteDTO aggregateWaste(Long canteenId, Long diningHallId, Long supplierId, LocalDate start, LocalDate end);
}
