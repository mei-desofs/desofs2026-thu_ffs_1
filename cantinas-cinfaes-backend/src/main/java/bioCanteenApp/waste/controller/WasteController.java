package bioCanteenApp.waste.controller;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.utils.exceptions.LogSanitizer;
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

        log.info("Fetching daily waste statistics");

        WasteDTO waste =
                wasteService.getDailyWaste();

        log.info("Daily waste statistics fetched successfully");

        return waste;
    }

    @GetMapping("/weekly")
    public WasteDTO getWeeklyWaste() {

        log.info("Fetching weekly waste statistics");

        WasteDTO waste =
                wasteService.getWeeklyWaste();

        log.info("Weekly waste statistics fetched successfully");

        return waste;
    }

    @GetMapping("/monthly")
    public WasteDTO getMonthlyWaste() {

        log.info("Fetching monthly waste statistics");

        WasteDTO waste =
                wasteService.getMonthlyWaste();

        log.info("Monthly waste statistics fetched successfully");

        return waste;
    }

    @GetMapping("/all")
    public WasteDTO getAllWaste() {

        log.info("Fetching overall waste statistics");

        WasteDTO waste =
                wasteService.getAllWaste();

        log.info("Overall waste statistics fetched successfully");

        return waste;
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
                "Fetching waste KPIs for user id: {} and period: {}",
                LogSanitizer.sanitize(userId),
                LogSanitizer.sanitize(period)
        );

        User user = userRepository.findById(userId);

        if (user == null) {

            log.warn(
                    "Waste KPI request failed. User not found with id: {}",
                    LogSanitizer.sanitize(userId)
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Utilizador não encontrado"
            );
        }

        LocalDate[] range =
                wasteService.getDateRange(period);

        log.info(
                "Calculating waste KPIs for role: {} between {} and {}",
                LogSanitizer.sanitize(user.getRole()),
                range[0],
                range[1]
        );

        switch (user.getRole()) {

            case CANTEEN_MANAGER:

                log.info(
                        "Generating waste KPIs for canteen id: {}",
                        LogSanitizer.sanitize(user.getCanteen().getId())
                );

                return wasteService.aggregateWaste(
                        user.getCanteen().getId(),
                        null,
                        null,
                        range[0],
                        range[1]
                );

            case DINING_HALL_MANAGER:

                log.info(
                        "Generating waste KPIs for dining hall id: {}",
                        LogSanitizer.sanitize(user.getDiningHall().getId())
                );

                return wasteService.aggregateWaste(
                        null,
                        user.getDiningHall().getId(),
                        null,
                        range[0],
                        range[1]
                );

            case NETWORK_MANAGER:

                log.info(
                        "Generating network waste KPIs with filters - canteenId: {}, diningHallId: {}, supplierId: {}",
                        LogSanitizer.sanitize(canteenId),
                        LogSanitizer.sanitize(diningHallId),
                        LogSanitizer.sanitize(supplierId)
                );

                return wasteService.aggregateWaste(
                        canteenId,
                        diningHallId,
                        supplierId,
                        range[0],
                        range[1]
                );

            default:

                log.warn(
                        "Unsupported role {} for user id: {}",
                        LogSanitizer.sanitize(user.getRole()),
                        LogSanitizer.sanitize(userId)
                );

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role não suportada"
                );
        }
    }
}