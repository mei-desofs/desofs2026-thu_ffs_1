package bioCanteenApp.dish.controller;

import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.service.IDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dishes")
@Slf4j
public class DishController {

    private final IDishService dishService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DishDto> createDish(@RequestBody DishDto dto) {

        log.info("Creating dish with name: {}", dto.getDishName());

        DishDto createdDish =
                dishService.createDish(dto);

        log.info(
                "Dish created successfully with id: {}",
                createdDish.getId()
        );

        return ResponseEntity.ok(createdDish);
    }

    @GetMapping(value = "/dish-types", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getDishTypes() {

        log.info("Fetching all dish types");

        List<String> dishTypes = dishService.getDishType()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        log.info("Found {} dish types", dishTypes.size());

        return ResponseEntity.ok(dishTypes);
    }

    @PostMapping(value = "/nutrition-allergens", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDishDTO> generateDishInformation(
            @RequestBody GetDishDTO dto
    ) {

        log.info("Generating nutrition and allergen information for dish");

        GetDishDTO generatedInfo =
                dishService.generateDishInformation(dto);

        log.info("Dish nutrition and allergen information generated successfully");

        return ResponseEntity.ok(generatedInfo);
    }

    @GetMapping(value = "/alternatives/{menuEntryDishId}", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DishDto>> getAlternatives(
            @PathVariable("menuEntryDishId") Long menuEntryDishId
    ) {

        log.info(
                "Fetching dish alternatives for menu entry dish id: {}",
                menuEntryDishId
        );

        List<DishDto> alternatives =
                dishService.getAlternatives(menuEntryDishId);

        log.info(
                "Found {} alternatives for menu entry dish id: {}",
                alternatives.size(),
                menuEntryDishId
        );

        return ResponseEntity.ok(alternatives);
    }

    @PutMapping(value = "/{menuEntryDishId}/replace", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> replaceDish(
            @PathVariable("menuEntryDishId") Long menuEntryDishId,
            @RequestParam("newDishId") Long newDishId
    ) {

        log.warn(
                "Replacing dish in menu entry dish id: {} with new dish id: {}",
                menuEntryDishId,
                newDishId
        );

        dishService.replaceDish(menuEntryDishId, newDishId);

        log.info(
                "Dish replaced successfully for menu entry dish id: {}",
                menuEntryDishId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/organic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getOrganicProducts() {

        log.info("Fetching organic products percentage");

        Double organicProducts =
                dishService.getOrganicProducts();

        log.info(
                "Organic products percentage calculated successfully: {}",
                organicProducts
        );

        return ResponseEntity.ok(organicProducts);
    }
}