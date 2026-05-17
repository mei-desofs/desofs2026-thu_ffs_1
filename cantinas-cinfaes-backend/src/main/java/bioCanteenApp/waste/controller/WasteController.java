package bioCanteenApp.waste.controller;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.service.IWasteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waste")
@Slf4j
public class WasteController {

    private final IWasteService wasteService;
    private final UserRepo userRepository;

    @GetMapping("/daily")
    public WasteDTO getDailyWaste() {

        log.info("Fetching daily waste report");

        return wasteService.getDailyWaste();
    }

    @GetMapping("/weekly")
    public WasteDTO getWeeklyWaste() {

        log.info("Fetching weekly waste report");

        return wasteService.getWeeklyWaste();
    }

    @GetMapping("/monthly")
    public WasteDTO getMonthlyWaste() {

        log.info("Fetching monthly waste report");

        return wasteService.getMonthlyWaste();
    }

    @GetMapping("/all")
    public WasteDTO getAllWaste() {

        log.info("Fetching all waste data");

        return wasteService.getAllWaste();
    }

    @GetMapping("/kpis/{period}")
    public WasteDTO getKPIs(
            @PathVariable("period") String period,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "canteenId", required = false) Long canteenId,
            @RequestParam(value = "diningHallId", required = false) Long diningHallId,
            @RequestParam(value = "supplierId", required = false) Long supplierId
    ) {

        log.info(
                "Fetching waste KPIs for period: {}, requested by user id: {}",
                period,
                userId
        );

        User user = userRepository.findById(userId);

        if (user == null) {

            log.warn(
                    "Waste KPI request failed because user id {} was not found",
                    userId
            );

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizador não encontrado");
        }

        LocalDate[] range = wasteService.getDateRange(period);

        log.info(
                "Waste KPI date range resolved for period {}: {} to {}",
                period,
                range[0],
                range[1]
        );

        switch (user.getRole()) {
            case CANTEEN_MANAGER:

                log.info(
                        "Aggregating waste KPIs for canteen manager user id: {}, canteen id: {}",
                        userId,
                        user.getCanteen().getId()
                );

                return wasteService.aggregateWaste(user.getCanteen().getId(), null, null, range[0], range[1]);

            case DINING_HALL_MANAGER:

                log.info(
                        "Aggregating waste KPIs for dining hall manager user id: {}, dining hall id: {}",
                        userId,
                        user.getDiningHall().getId()
                );

                return wasteService.aggregateWaste(null, user.getDiningHall().getId(), null, range[0], range[1]);

            case NETWORK_MANAGER:

                log.info(
                        "Aggregating waste KPIs for network manager user id: {}, canteen id: {}, dining hall id: {}, supplier id: {}",
                        userId,
                        canteenId,
                        diningHallId,
                        supplierId
                );

                return wasteService.aggregateWaste(canteenId, diningHallId, supplierId, range[0], range[1]);

            default:

                log.warn(
                        "Waste KPI request failed due to unsupported role for user id: {}",
                        userId
                );

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role não suportada");
        }
    }
}