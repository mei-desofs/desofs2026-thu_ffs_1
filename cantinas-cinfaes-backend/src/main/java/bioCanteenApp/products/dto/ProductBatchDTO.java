package bioCanteenApp.products.dto;

import bioCanteenApp.products.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBatchDTO {
    private Long id;
    private Long productId;
    private Double quantity;
    private LocalDate receivedDate;
    private LocalDate expirationDate;
    private boolean isBio;
    private boolean isQuarantined;
    private Long supplierId;
}
