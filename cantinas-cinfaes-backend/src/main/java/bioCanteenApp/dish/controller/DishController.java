package bioCanteenApp.dish.controller;

import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dishes")
public class DishController {
    private final IDishService dishService;

    @PostMapping
    public ResponseEntity<DishDto> createDish(@RequestBody DishDto dto) {
        DishDto createdDish = dishService.createDish(dto);
        return ResponseEntity.ok(createdDish);
    }

    @GetMapping("/dish-types")
    public ResponseEntity<List<String>> getDishTypes() {
        List<String> dishTypes = dishService.getDishType()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dishTypes);
    }

    // POST Generate Dish Allergens and Nutrition Information
    @PostMapping("/nutrition-allergens")
    public ResponseEntity<GetDishDTO> generateDishInformation(@RequestBody GetDishDTO dto) {
        GetDishDTO generatedInfo = dishService.generateDishInformation(dto);
        return ResponseEntity.ok(generatedInfo);
    }

    @GetMapping("/alternatives/{menuEntryDishId}")
    public ResponseEntity<List<DishDto>> getAlternatives(@PathVariable("menuEntryDishId") Long menuEntryDishId) {
        List<DishDto> alternatives = dishService.getAlternatives(menuEntryDishId);
        return ResponseEntity.ok(alternatives);
    }

    @PutMapping("/{menuEntryDishId}/replace")
    public ResponseEntity<Void> replaceDish(
            @PathVariable("menuEntryDishId") Long menuEntryDishId,
            @RequestParam("newDishId") Long newDishId
    ) {
        dishService.replaceDish(menuEntryDishId, newDishId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organic")
    public ResponseEntity<Double> getOrganicProducts() {
        Double organicProducts = dishService.getOrganicProducts();
        return ResponseEntity.ok(organicProducts);
    }
}
