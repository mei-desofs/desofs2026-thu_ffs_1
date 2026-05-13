package bioCanteenApp.suppliers.service;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.mapper.ISupplierMapper;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.repository.IUserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService {

    private final ISupplierMapper supplierMapper;
    private final ISupplierRepo supplierRepo;
    private final IProductRepo productRepo;

    @Override
    public SupplierApplicationDTO applyToSupplierPosition(SupplierApplicationDTO dto) {

        List<SupplierCapacity> capacities = dto.getSupplierCapacity().stream()
                .map(c -> new SupplierCapacity(
                        c.getProductName(),
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getQuantity()
                ))
                .collect(Collectors.toList());

        SupplierApplication application = supplierMapper.toDomain(dto);

        application.setSupplierCapacity(capacities);
        supplierRepo.save(application);
        return supplierMapper.toDTO(application);
    }

    @Override
    @Transactional
    public SupplierDTO approveSupplier(SupplierDTO dto) {

        SupplierApplication application = supplierRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Application not found for email: " + dto.getEmail()));

        application.setStatus(SupplierApplicationStatus.APPROVED);
        supplierRepo.save(application);

        Optional<Supplier> supplier = supplierRepo.findAll().stream()
                .filter(s -> s.getUser().getEmail().equals(dto.getEmail()))
                .findFirst();

        return supplierMapper.toDTO(supplier.get());
    }

    @Override
    @Transactional
    public SupplierDTO rejectSupplier(SupplierDTO dto) {

        SupplierApplication application = supplierRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Application not found for email: " + dto.getEmail()));

        application.setStatus(SupplierApplicationStatus.REJECTED);
        supplierRepo.save(application);

        Optional<Supplier> supplier = supplierRepo.findAll().stream()
                .filter(s -> s.getUser().getEmail().equals(dto.getEmail()))
                .findFirst();

        return supplierMapper.toDTO(supplier.get());
    }

    @Override
    public Map<String, Long> getSupplierStats() {
        long totalSuppliers = supplierRepo.countTotalSuppliers();
        long approvedApplications = supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.APPROVED);
        long pendingApplications = supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.PENDING);
        long rejectedApplications = supplierRepo.countApplicationsByStatus(SupplierApplicationStatus.REJECTED);

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalSuppliers", totalSuppliers);
        stats.put("approvedSuppliers", approvedApplications);
        stats.put("pendingSuppliers", pendingApplications);
        stats.put("rejectedSuppliers", rejectedApplications);

        return stats;
    }

    @Override
    public List<SupplierDTO> findAllSuppliers() {
        return supplierRepo.findAll()
                .stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDTO> findAllSuppliersByOrderByProduct(Long id) {
        Product product = productRepo.findById(id);

        List<Supplier> suppliers = supplierRepo.findAllSuppliersByOrderByProduct(product);

        return suppliers.stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierApplicationDTO> findAllApplications() {
        return supplierRepo.findAllApplications()
                .stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SupplierDTO quarantineSupplier(SupplierDTO dto) {

        Supplier supplier = supplierRepo.findBySupplierEmail(dto.getEmail());

        supplier.setQuarantined(true);
        supplierRepo.save(supplier);

        List<ProductBatch> productBatches =
                productRepo.findProductsBySupplier(supplier);

        for (ProductBatch batch : productBatches) {
            batch.setQuarantined(true);
            productRepo.save(batch);
        }

        return supplierMapper.toDTO(supplier);
    }

    @Transactional
    @Override
    public SupplierDTO unquarantineSupplier(SupplierDTO dto) {

        Supplier supplier = supplierRepo.findBySupplierEmail(dto.getEmail());

        supplier.setQuarantined(false);
        supplierRepo.save(supplier);

        List<ProductBatch> productBatches =
                productRepo.findProductsBySupplier(supplier);

        for (ProductBatch batch : productBatches) {
            batch.setQuarantined(false);
            productRepo.save(batch);
        }

        return supplierMapper.toDTO(supplier);
    }

    @Override
    public List<SupplierDTO> getSuppliersByName(String name) {
        List<Supplier> suppliers = supplierRepo.findSuppliersByName(name);
        return suppliers.stream().map(supplierMapper::toDTO).toList();
    }

    @Override
    public List<SupplierDTO> getSuppliersByVillage(String village) {
        List<Supplier> suppliers = supplierRepo.findSuppliersByVillage(village);
        return suppliers.stream().map(supplierMapper::toDTO).toList();
    }

    @Override
    public List<SupplierDTO> getSuppliersByMunicipality(String municipality) {
        List<Supplier> suppliers = supplierRepo.findSuppliersByMunicipality(municipality);
        return suppliers.stream().map(supplierMapper::toDTO).toList();
    }

}
