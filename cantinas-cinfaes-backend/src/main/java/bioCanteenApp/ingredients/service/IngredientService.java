package bioCanteenApp.ingredients.service;

import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.mapper.IngredientMapper;
import bioCanteenApp.ingredients.repository.IngredientRepo;
import bioCanteenApp.products.domain.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static bioCanteenApp.products.domain.Season.fromMonth;

@Service
@RequiredArgsConstructor
public class IngredientService implements IIngredientService {

    private final IngredientRepo ingredientRepository;

    private final IngredientMapper ingredientMapper;

    @Override
    public List<IngredientDto> getAllIngredients() {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        return ingredients.stream().map(ingredientMapper::toDTO).toList();
    }

    @Override
    public List<IngredientDto> getSeasonalIngredients() {
        Month currentMonth = LocalDate.now().getMonth();
        Season currentSeason = fromMonth(currentMonth);

        List<Ingredient> seasonalIngredients = ingredientRepository.findAll().stream()
                .filter(ingredient -> ingredient.getProduct() != null
                        && ingredient.getProduct().getSeasons() != null
                        && ingredient.getProduct().getSeasons().contains(currentSeason))
                .toList();

        return seasonalIngredients.stream()
                .map(ingredientMapper::toDTO)
                .toList();
    }

    @Override
    public Long getIngredientCount() {
        return ingredientRepository.count();
    }
}
