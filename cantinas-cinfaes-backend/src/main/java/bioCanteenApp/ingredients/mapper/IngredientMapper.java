package bioCanteenApp.ingredients.mapper;

import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.dto.IngredientDto;
import bioCanteenApp.products.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

        public IngredientDto toDTO(Ingredient ingredient) {
            if (ingredient == null) return null;

            Long productId = (ingredient.getProduct() != null) ? ingredient.getProduct().getId() : null;

            return new IngredientDto(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    productId
            );
        }

        public Ingredient toDomain(IngredientDto ingredientDto) {
            if (ingredientDto == null) return null;

            return new Ingredient(
                    ingredientDto.getName(),
                    ingredientDto.getQuantity(),
                    new Product(ingredientDto.getProductId())
            );
        }
}

