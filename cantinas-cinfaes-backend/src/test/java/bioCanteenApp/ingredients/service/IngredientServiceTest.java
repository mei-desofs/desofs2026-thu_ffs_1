package bioCanteenApp.ingredients.service;

import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.mapper.IngredientMapper;
import bioCanteenApp.ingredients.repository.IngredientRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static bioCanteenApp.products.domain.Season.fromMonth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngredientServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IngredientRepo ingredientRepository;
    private IngredientMapper ingredientMapper;

    private IngredientService service;

    @BeforeEach
    void setUp() {
        ingredientRepository = mock(IngredientRepo.class);
        ingredientMapper = mock(IngredientMapper.class);

        service = new IngredientService(
                ingredientRepository,
                ingredientMapper
        );
    }

    @Test
    void shouldGetAllIngredients() {
        Ingredient ingredient1 = createIngredient("Rice");
        Ingredient ingredient2 = createIngredient("Fish");

        IngredientDto dto1 = new IngredientDto();
        IngredientDto dto2 = new IngredientDto();

        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredient1, ingredient2));

        when(ingredientMapper.toDTO(ingredient1))
                .thenReturn(dto1);

        when(ingredientMapper.toDTO(ingredient2))
                .thenReturn(dto2);

        List<IngredientDto> result =
                service.getAllIngredients();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(ingredientRepository).findAll();
        verify(ingredientMapper).toDTO(ingredient1);
        verify(ingredientMapper).toDTO(ingredient2);
    }

    @Test
    void shouldGetSeasonalIngredients() {
        Season currentSeason =
                fromMonth(LocalDate.now().getMonth());

        Product seasonalProduct = new Product(
                "Rice",
                "kg",
                30,
                List.of(currentSeason),
                List.of(Allergen.GLUTEN)
        );

        Product nonSeasonalProduct = new Product(
                "Fish",
                "kg",
                10,
                List.of(getDifferentSeason(currentSeason)),
                List.of(Allergen.FISH)
        );

        Ingredient seasonalIngredient =
                new Ingredient(
                        "Rice Ingredient",
                        1.0,
                        seasonalProduct
                );

        Ingredient nonSeasonalIngredient =
                new Ingredient(
                        "Fish Ingredient",
                        2.0,
                        nonSeasonalProduct
                );

        IngredientDto dto = new IngredientDto();

        when(ingredientRepository.findAll())
                .thenReturn(List.of(
                        seasonalIngredient,
                        nonSeasonalIngredient
                ));

        when(ingredientMapper.toDTO(seasonalIngredient))
                .thenReturn(dto);

        List<IngredientDto> result =
                service.getSeasonalIngredients();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));

        verify(ingredientRepository).findAll();
        verify(ingredientMapper).toDTO(seasonalIngredient);
        verify(ingredientMapper, never())
                .toDTO(nonSeasonalIngredient);
    }

    @Test
    void shouldIgnoreIngredientWithoutProductWhenGettingSeasonalIngredients() {
        Ingredient ingredientWithoutProduct =
                new Ingredient(
                        "Unknown Ingredient",
                        1.0,
                        null
                );

        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredientWithoutProduct));

        List<IngredientDto> result =
                service.getSeasonalIngredients();

        assertTrue(result.isEmpty());

        verify(ingredientRepository).findAll();
        verify(ingredientMapper, never()).toDTO(any());
    }

    @Test
    void shouldIgnoreIngredientWithProductWithoutSeasonsWhenGettingSeasonalIngredients() {
        Product product = new Product(
                "Rice",
                "kg",
                30,
                null,
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient =
                new Ingredient(
                        "Rice Ingredient",
                        1.0,
                        product
                );

        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredient));

        List<IngredientDto> result =
                service.getSeasonalIngredients();

        assertTrue(result.isEmpty());

        verify(ingredientRepository).findAll();
        verify(ingredientMapper, never()).toDTO(any());
    }

    @Test
    void shouldReturnEmptySeasonalIngredientsWhenNoIngredientsMatchSeason() {
        Season currentSeason =
                fromMonth(LocalDate.now().getMonth());

        Product product = new Product(
                "Fish",
                "kg",
                10,
                List.of(getDifferentSeason(currentSeason)),
                List.of(Allergen.FISH)
        );

        Ingredient ingredient =
                new Ingredient(
                        "Fish Ingredient",
                        2.0,
                        product
                );

        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredient));

        List<IngredientDto> result =
                service.getSeasonalIngredients();

        assertTrue(result.isEmpty());

        verify(ingredientRepository).findAll();
        verify(ingredientMapper, never()).toDTO(any());
    }

    @Test
    void shouldGetIngredientCount() {
        when(ingredientRepository.count())
                .thenReturn(25L);

        Long result = service.getIngredientCount();

        assertEquals(25L, result);

        verify(ingredientRepository).count();
    }

    private Ingredient createIngredient(String name) {
        Product product = new Product(
                name + " Product",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        return new Ingredient(
                name,
                1.0,
                product
        );
    }

    private Season getDifferentSeason(Season season) {
        return switch (season) {
            case SPRING -> Season.SUMMER;
            case SUMMER -> Season.AUTUMN;
            case AUTUMN -> Season.WINTER;
            case WINTER -> Season.SPRING;
        };
    }
}