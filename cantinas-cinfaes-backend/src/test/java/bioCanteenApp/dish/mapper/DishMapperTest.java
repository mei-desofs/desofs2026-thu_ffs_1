package bioCanteenApp.dish.mapper;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishIngredient;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.recipes.repository.IRecipeRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishMapperTest {

    @Mock
    private IRecipeRepo recipeRepository;

    @Mock
    private IDishRepo dishRepository;

    @Mock
    private IIngredientRepo ingredientRepository;

    @InjectMocks
    private DishMapper mapper;

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_withNoIngredients_mapsFields() {
        Dish dish = new Dish("Frango Assado", DishType.MEAT);
        dish.setId(1L);
        dish.setNutritionalInformation("500kcal");

        DishDto dto = mapper.toDTO(dish);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Frango Assado", dto.getDishName());
        assertEquals("500kcal", dto.getNutritionalInformation());
        assertEquals("MEAT", dto.getDishType());
        assertNull(dto.getRecipeId());
        assertTrue(dto.getIngredients().isEmpty());
    }

    @Test
    void toDTO_withNullDishType_mapsNullDishType() {
        Dish dish = new Dish();
        dish.setDishName("Prato");
        dish.setDishType(null);

        DishDto dto = mapper.toDTO(dish);

        assertNull(dto.getDishType());
    }

    @Test
    void toDTO_withIngredients_mapsIngredientDtos() {
        Product product = new Product("Arroz", "g", 30, null, null);
        product.setId(10L);
        Ingredient ingredient = new Ingredient("Arroz cozido", 100.0, product);
        ingredient.setId(5L);

        Dish dish = new Dish("Arroz c/ Frango", DishType.MEAT);
        dish.setId(2L);
        dish.addIngredient(ingredient, 200.0);

        DishDto dto = mapper.toDTO(dish);

        assertEquals(1, dto.getIngredients().size());
        assertEquals(5L, dto.getIngredients().get(0).getIngredientId());
        assertEquals(10L, dto.getIngredients().get(0).getProductId());
        assertEquals("Arroz cozido", dto.getIngredients().get(0).getIngredientName());
        assertEquals(200.0, dto.getIngredients().get(0).getQuantity());
        assertEquals("g", dto.getIngredients().get(0).getUnit());
    }

    @Test
    void toDomain_withNull_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_withoutId_createsNewDish() {
        DishDto dto = DishDto.builder()
                .dishName("Sopa")
                .nutritionalInformation("150kcal")
                .build();

        Dish dish = mapper.toDomain(dto);

        assertNotNull(dish);
        assertEquals("Sopa", dish.getDishName());
        assertEquals("150kcal", dish.getNutritionalInformation());
        assertNull(dish.getDishType());
    }

    @Test
    void toDomain_withDishType_setsDishType() {
        DishDto dto = DishDto.builder()
                .dishName("Peixe Grelhado")
                .dishType("FISH")
                .build();

        Dish dish = mapper.toDomain(dto);

        assertEquals(DishType.FISH, dish.getDishType());
    }

    @Test
    void toDomain_withId_lookupsDishInRepo() {
        Dish existing = new Dish("Existente", DishType.VEGETARIAN);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(existing));

        DishDto dto = DishDto.builder()
                .id(1L)
                .dishName("Updated Name")
                .build();

        Dish result = mapper.toDomain(dto);

        assertEquals("Updated Name", result.getDishName());
        verify(dishRepository).findById(1L);
    }

    @Test
    void toDomain_withUnknownId_createsNewDish() {
        when(dishRepository.findById(99L)).thenReturn(Optional.empty());

        DishDto dto = DishDto.builder()
                .id(99L)
                .dishName("Novo Prato")
                .build();

        Dish result = mapper.toDomain(dto);

        assertEquals("Novo Prato", result.getDishName());
    }
}
