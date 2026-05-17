package bioCanteenApp.ingredients.controller;

import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredients")
@Slf4j
public class IngredientController {

    private final IngredientService ingredientsService;

    @GetMapping("/seasonal")
    public List<IngredientDto> getSeasonalIngredients() {

        log.info("Fetching seasonal ingredients");

        return ingredientsService.getSeasonalIngredients();
    }

    @GetMapping("/all")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {

        log.info("Fetching all ingredients");

        List<IngredientDto> products =
                ingredientsService.getAllIngredients();

        log.info("Found {} ingredients", products.size());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Long> getIngredientCount() {

        log.info("Fetching ingredient statistics");

        Long count = ingredientsService.getIngredientCount();

        log.info("Current ingredient count: {}", count);

        return ResponseEntity.ok(count);
    }
}