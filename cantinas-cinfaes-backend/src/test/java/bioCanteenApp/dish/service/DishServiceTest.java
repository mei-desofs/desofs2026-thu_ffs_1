package bioCanteenApp.dish.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.mapper.DishMapper;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
import bioCanteenApp.menu.repository.MenuRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.recipes.repository.IRecipeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishServiceTest {

    private IDishRepo dishRepo;
    private IIngredientRepo ingredientRepo;
    private IProductRepo productRepo;
    private IProductRepo productBatchRepo;
    private IRecipeRepo recipeRepo;

    private DishService service;

    @BeforeEach
    void setUp() {
        dishRepo = mock(IDishRepo.class);
        ingredientRepo = mock(IIngredientRepo.class);
        productRepo = mock(IProductRepo.class);
        productBatchRepo = mock(IProductRepo.class);
        recipeRepo = mock(IRecipeRepo.class);

        DishMapper dishMapper = new DishMapper(
                recipeRepo,
                dishRepo,
                ingredientRepo
        );

        service = new DishService(
                dishRepo,
                ingredientRepo,
                productRepo,
                dishMapper,
                null,
                productBatchRepo
        );
    }

    @Test
    void shouldGenerateDishInformationWithAllergens() {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName("Rice");

        GetDishDTO dto = new GetDishDTO();
        dto.setIngredients(List.of(ingredientDto));

        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        when(ingredientRepo.findByName("Rice")).thenReturn(List.of(ingredient));

        GetDishDTO result = service.generateDishInformation(dto);

        assertNotNull(result.getAllergens());
        assertTrue(result.getAllergens().contains("Glúten"));
        assertTrue(result.getNutritionalInformation().contains("hidratos de carbono"));
    }

    @Test
    void shouldGenerateDishInformationWithoutAllergens() {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName("Vegetable");

        GetDishDTO dto = new GetDishDTO();
        dto.setIngredients(List.of(ingredientDto));

        Product product = new Product(
                "Vegetable",
                "kg",
                30,
                List.of(Season.SPRING),
                List.of()
        );

        Ingredient ingredient = new Ingredient("Vegetable", 1.0, product);

        when(ingredientRepo.findByName("Vegetable")).thenReturn(List.of(ingredient));

        GetDishDTO result = service.generateDishInformation(dto);

        assertTrue(result.getAllergens().isEmpty());
        assertEquals(
                "Refeição leve e equilibrada, adequada a uma dieta variada.",
                result.getNutritionalInformation()
        );
    }

    @Test
    void shouldGetDishesWithSeasonalIngredients() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        Dish dish = new Dish("Vegetarian Dish", DishType.VEGETARIAN);
        dish.addIngredient(ingredient, 1.0);

        when(dishRepo.findAll()).thenReturn(List.of(dish));

        var result = service.getDishesWithSeasonalIngredients(List.of("Rice"));

        assertEquals(1, result.size());
    }

    @Test
    void shouldNotReturnDishWhenIngredientIsNotSeasonal() {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        Ingredient ingredient = new Ingredient("Rice", 1.0, product);

        Dish dish = new Dish("Vegetarian Dish", DishType.VEGETARIAN);
        dish.addIngredient(ingredient, 1.0);

        when(dishRepo.findAll()).thenReturn(List.of(dish));

        var result = service.getDishesWithSeasonalIngredients(List.of("Tomato"));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetDishTypes() {
        List<DishType> result = service.getDishType();

        assertEquals(List.of(DishType.values()), result);
    }

    @Test
    void shouldGetOrganicProducts() {
        when(productRepo.getOrganicProducts()).thenReturn(75.5);

        Double result = service.getOrganicProducts();

        assertEquals(75.5, result);
    }
}