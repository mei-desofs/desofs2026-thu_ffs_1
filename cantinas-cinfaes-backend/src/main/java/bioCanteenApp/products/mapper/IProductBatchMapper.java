package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;

public interface IProductBatchMapper {
    ProductBatchDTO toDTO(ProductBatch productBatch);
    ProductBatch toDomain(ProductBatchDTO dto);
}
