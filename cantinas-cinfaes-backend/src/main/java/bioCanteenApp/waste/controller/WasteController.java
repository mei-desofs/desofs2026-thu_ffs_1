package bioCanteenApp.waste.controller;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.service.IWasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waste")
public class WasteController {

    private final IWasteService wasteService;
    private final UserRepo userRepository;

    @GetMapping("/daily")
    public WasteDTO getDailyWaste() {
        return wasteService.getDailyWaste();
    }

    @GetMapping("/weekly")
    public WasteDTO getWeeklyWaste() {
        return wasteService.getWeeklyWaste();
    }

    @GetMapping("/monthly")
    public WasteDTO getMonthlyWaste() {
        return wasteService.getMonthlyWaste();
    }

    @GetMapping("/all")
    public WasteDTO getAllWaste() {
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

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizador não encontrado");
        }

        LocalDate[] range = wasteService.getDateRange(period);

        switch (user.getRole()) {
            case CANTEEN_MANAGER:
                return wasteService.aggregateWaste(user.getCanteen().getId(), null, null, range[0], range[1]);
            case DINING_HALL_MANAGER:
                return wasteService.aggregateWaste(null, user.getDiningHall().getId(), null, range[0], range[1]);
            case NETWORK_MANAGER:
                return wasteService.aggregateWaste(canteenId, diningHallId, supplierId, range[0], range[1]);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role não suportada");
        }
    }
}