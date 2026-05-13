package bioCanteenApp.products.mapper;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.repository.IProductBatchRepo;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductBatchMapper implements IProductBatchMapper {

    private final IProductMapper productMapper;
    private final IProductBatchRepo productBatchRepository;
    private final IProductRepo productRepository;
    private final ISupplierRepo supplierRepository;

    @Override
    public ProductBatchDTO toDTO(ProductBatch productBatch) {
        if (productBatch == null) {
            return null;
        }

        return ProductBatchDTO.builder()
                .id(productBatch.getId() != null ? productBatch.getId() : 0)
                .productId(productBatch.getId())
                .quantity(productBatch.getQuantity())
                .receivedDate(productBatch.getReceivedDate())
                .expirationDate(productBatch.getExpirationDate())
                .build();
    }

    @Override
    public ProductBatch toDomain(ProductBatchDTO dto) {
        if (dto == null) {
            return null;
        }

        ProductBatch existingBatch = productBatchRepository.findById(dto.getId());
        if (existingBatch != null) {
            return existingBatch;
        }

        Product product = productRepository.findById(dto.getProductId());
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product with id " + dto.getProductId() + " not found."
            );
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId());

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier with id " + dto.getSupplierId() + " not found."
            );
        }

        return new ProductBatch(
                product,
                dto.getQuantity(),
                dto.getReceivedDate(),
                dto.isBio(),
                supplier
        );
    }
}
