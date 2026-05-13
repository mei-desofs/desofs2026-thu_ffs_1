package bioCanteenApp.products.service;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.dto.ProductBatchDTO;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.mapper.IProductBatchMapper;
import bioCanteenApp.products.mapper.IProductMapper;
import bioCanteenApp.products.repository.IProductBatchRepo;
import bioCanteenApp.products.repository.IProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductBatchService implements IProductBatchService {
    private final IProductBatchRepo productBatchRepository;
    private final IProductBatchMapper productBatchMapper;
    private final IProductRepo productRepo;

    public ProductBatchService(IProductBatchMapper productBatchMapper, IProductBatchRepo productBatchRepository, IProductRepo productRepo) {
        this.productBatchMapper = productBatchMapper;
        this.productBatchRepository = productBatchRepository;
        this.productRepo = productRepo;
    }

    @Override
    public List<ProductBatchDTO> getAllBatches() {
        List<ProductBatch> batches =
                (List<ProductBatch>) productBatchRepository.findAll();

        return batches.stream()
                .map(productBatchMapper::toDTO)
                .toList();
    }

    @Override
    public ProductBatchDTO getBatchById(Long id) {
        ProductBatch batch = productBatchRepository.findById(id);
        return productBatchMapper.toDTO(batch);
    }

    @Override
    public List<ProductBatchDTO> getBatchesByProduct(Long product) {

        Product prodEntity = productRepo.findById(product);

        if(prodEntity == null) {
            throw new IllegalArgumentException(
                    "Product with id " + product + " not found."
            );
        }

        List<ProductBatch> batches =
                productBatchRepository.findByProduct(prodEntity);

        return batches.stream()
                .map(productBatchMapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductBatchDTO> getValidBatchesByProduct(Long product) {

        Product prodEntity = productRepo.findById(product);

        if(prodEntity == null) {
            throw new IllegalArgumentException(
                    "Product with id " + product + " not found."
            );
        }

        List<ProductBatch> batches =
                productBatchRepository.findValidBatchesByProduct(prodEntity);

        return batches.stream()
                .map(productBatchMapper::toDTO)
                .toList();
    }

    @Override
    public double getValidStockByProduct(Long productId) {
        return productBatchRepository.sumValidStockByProduct(productId);
    }

    @Override
    @Transactional
    public ProductBatchDTO saveBatch(ProductBatchDTO productBatch) {

        ProductBatch productBatchEntity = productBatchMapper.toDomain(productBatch);

        ProductBatch savedBatch = productBatchRepository.save(productBatchEntity);

        return productBatchMapper.toDTO(savedBatch);
    }

    @Override
    public void deleteBatch(ProductBatchDTO productBatch) {
        ProductBatch productBatchEntity =
                productBatchMapper.toDomain(productBatch);
        productBatchRepository.delete(productBatchEntity);
    }
}
