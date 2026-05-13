package bioCanteenApp.canteens.service;

import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.mappers.ICanteenMapper;
import bioCanteenApp.canteens.repository.ICanteenRepo;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import static bioCanteenApp.utils.exceptions.CanteenExceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CanteenService implements ICanteenService {

    private final ICanteenRepo repo;
    private final ICanteenMapper mapper;
    private final ISupplierRepo supplierRepo;
    private final IProductRepo productRepo;

    public CanteenService(ICanteenRepo repo, ICanteenMapper mapper, ISupplierRepo supplierRepo, IProductRepo productRepo) {
        this.repo = repo;
        this.mapper = mapper;
        this.supplierRepo = supplierRepo;
        this.productRepo = productRepo;
    }

    public CanteenDTO createCanteen(CanteenDTO dto) {
        Canteen canteen = mapper.toDomain(dto);

        repo.findByName(canteen.getName()).ifPresent(existing -> {
            throw new CanteenAlreadyExists(canteen.getName());
        });

        Canteen saved = repo.save(canteen);
        return mapper.toDTO(saved);
    }


    public List<CanteenDTO> getAllCanteens() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public CanteenDTO getById(Long id) {
        Canteen canteen = repo.findById(id)
                .orElseThrow(() -> new CanteenNotFound(id));

        return mapper.toDTO(canteen);
    }

    @Transactional
    @Override
    public List<CanteenDTO> quarantineCanteensByVillage(String village) {

        Village villageEnum = Village.valueOf(village.toUpperCase());

        List<Canteen> canteens = repo.getAllCanteensByVillage(String.valueOf(villageEnum));

        for (Canteen canteen : canteens) {
            canteen.setIsQuarantined(true);
        }

        List<Supplier> suppliers = supplierRepo.getAllSuppliersByVillage(String.valueOf(villageEnum));
        for (Supplier supplier : suppliers) {
            supplier.setQuarantined(true);

            List<ProductBatch> productBatches =
                    productRepo.findProductsBySupplier(supplier);

            for (ProductBatch batch : productBatches) {
                batch.setQuarantined(true);
            }
        }

        return canteens.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public List<CanteenDTO> unquarantineCanteensByVillage(String village) {
        Village villageEnum = Village.valueOf(village.toUpperCase());

        List<Canteen> canteens = repo.getAllCanteensByVillage(String.valueOf(villageEnum));

        for (Canteen canteen : canteens) {
            canteen.setIsQuarantined(false);
        }

        List<Supplier> suppliers = supplierRepo.getAllSuppliersByVillage(String.valueOf(villageEnum));
        for (Supplier supplier : suppliers) {
            supplier.setQuarantined(false);

            List<ProductBatch> productBatches =
                    productRepo.findProductsBySupplier(supplier);

            for (ProductBatch batch : productBatches) {
                batch.setQuarantined(false);
            }
        }

        return canteens.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<CanteenDTO> getByMunicipality(String municipality) {
        List<Canteen> canteens = repo.getAllCanteensByMunicipality(municipality);
        return canteens.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<CanteenDTO> getByVillage(String village) {
        List<Canteen> canteens = repo.getAllCanteensByVillage(village);
         return canteens.stream()
                .map(mapper::toDTO)
                .toList();
    }

}
