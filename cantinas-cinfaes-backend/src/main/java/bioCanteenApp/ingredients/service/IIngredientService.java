package bioCanteenApp.ingredients.service;

import bioCanteenApp.ingredients.dto.IngredientDto;

import java.util.List;

public interface IIngredientService {
    List<IngredientDto> getAllIngredients();

    // Get ingredients whose associated products are in season for the current month
    List<IngredientDto> getSeasonalIngredients();

    Long getIngredientCount();
}
