package bioCanteenApp.ingredients.controller;

import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.service.IngredientService;
import bioCanteenApp.products.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientsService;

    @GetMapping("/seasonal")
    public List<IngredientDto> getSeasonalIngredients() {
        return ingredientsService.getSeasonalIngredients();
    }

    @GetMapping("/all")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        List<IngredientDto> products = ingredientsService.getAllIngredients();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Long> getIngredientCount() {
        Long count = ingredientsService.getIngredientCount();
        return ResponseEntity.ok(count);
    }

}
