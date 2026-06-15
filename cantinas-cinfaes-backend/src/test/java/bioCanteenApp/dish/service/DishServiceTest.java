package bioCanteenApp.dish.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.dto.GetDishDTO;
import bioCanteenApp.dish.mapper.DishMapper;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
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

    @Test
    void shouldThrowWhenIngredientIsNotFound() {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName("Unknown");

        GetDishDTO dto = new GetDishDTO();
        dto.setIngredients(List.of(ingredientDto));

        when(ingredientRepo.findByName("Unknown")).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generateDishInformation(dto)
        );

        assertEquals("Ingredient not found: Unknown", exception.getMessage());
    }

    @Test
    void shouldGenerateDishInformationWithMultipleAllergens() {
        IngredientDto fishDto = new IngredientDto();
        fishDto.setName("Salmon");

        IngredientDto milkDto = new IngredientDto();
        milkDto.setName("Milk");

        GetDishDTO dto = new GetDishDTO();
        dto.setIngredients(List.of(fishDto, milkDto));

        Product fishProduct = new Product(
                "Salmon",
                "kg",
                10,
                List.of(Season.SPRING),
                List.of(Allergen.FISH)
        );

        Product milkProduct = new Product(
                "Milk",
                "l",
                5,
                List.of(Season.SPRING),
                List.of(Allergen.MILK)
        );

        Ingredient fish = new Ingredient("Salmon", 1.0, fishProduct);
        Ingredient milk = new Ingredient("Milk", 1.0, milkProduct);

        when(ingredientRepo.findByName("Salmon")).thenReturn(List.of(fish));
        when(ingredientRepo.findByName("Milk")).thenReturn(List.of(milk));

        GetDishDTO result = service.generateDishInformation(dto);

        assertTrue(result.getAllergens().contains("Peixe"));
        assertTrue(result.getAllergens().contains("Leite"));
        assertTrue(result.getNutritionalInformation().contains("ómega-3"));
        assertTrue(result.getNutritionalInformation().contains("proteína e cálcio"));
    }

    @Test
    void shouldGetAllDishes() {
        Dish dish = new Dish("Soup", DishType.VEGETARIAN);

        when(dishRepo.findAll()).thenReturn(List.of(dish));

        List<DishDto> result = service.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnOnlyDishesWithAllIngredientsSeasonal() {
        Product riceProduct = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of()
        );

        Product tomatoProduct = new Product(
                "Tomato",
                "kg",
                10,
                List.of(Season.SPRING),
                List.of()
        );

        Ingredient rice = new Ingredient("Rice", 1.0, riceProduct);
        Ingredient tomato = new Ingredient("Tomato", 1.0, tomatoProduct);

        Dish validDish = new Dish("Rice with Tomato", DishType.VEGETARIAN);
        validDish.addIngredient(rice, 1.0);
        validDish.addIngredient(tomato, 1.0);

        Product potatoProduct = new Product(
                "Potato",
                "kg",
                20,
                List.of(Season.WINTER),
                List.of()
        );

        Ingredient potato = new Ingredient("Potato", 1.0, potatoProduct);

        Dish invalidDish = new Dish("Rice with Potato", DishType.VEGETARIAN);
        invalidDish.addIngredient(rice, 1.0);
        invalidDish.addIngredient(potato, 1.0);

        when(dishRepo.findAll()).thenReturn(List.of(validDish, invalidDish));

        List<DishDto> result = service.getDishesWithSeasonalIngredients(
                List.of("Rice", "Tomato")
        );

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowWhenCreatingDishWithoutEnoughStock() {
        DishIngredientDto ingredient = new DishIngredientDto();
        ingredient.setProductId(1L);
        ingredient.setIngredientName("Rice");
        ingredient.setQuantity(5.0);

        DishDto dto = new DishDto();
        dto.setIngredients(List.of(ingredient));

        when(productBatchRepo.sumValidStockByProduct(1L)).thenReturn(2.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createDish(dto)
        );

        assertEquals(
                "Dish ingredients do not have enough available stock.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenCreatingDishWithoutProteinSource() {
        DishIngredientDto ingredient = new DishIngredientDto();
        ingredient.setProductId(1L);
        ingredient.setIngredientName("Rice");
        ingredient.setQuantity(1.0);

        DishDto dto = new DishDto();
        dto.setIngredients(List.of(ingredient));

        when(productBatchRepo.sumValidStockByProduct(1L)).thenReturn(10.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createDish(dto)
        );

        assertEquals(
                "Dish must contain a protein source.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenCreatingDishWithoutSideDish() {
        DishIngredientDto ingredient = new DishIngredientDto();
        ingredient.setProductId(1L);
        ingredient.setIngredientName("Chicken");
        ingredient.setQuantity(1.0);

        DishDto dto = new DishDto();
        dto.setIngredients(List.of(ingredient));

        when(productBatchRepo.sumValidStockByProduct(1L)).thenReturn(10.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createDish(dto)
        );

        assertEquals(
                "Dish must contain a side dish.",
                exception.getMessage()
        );
    }
}