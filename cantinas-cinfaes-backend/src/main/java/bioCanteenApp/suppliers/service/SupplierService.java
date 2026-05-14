package bioCanteenApp.suppliers.service;

import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.email.validator.EmailDomainValidator;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.suppliers.mapper.ISupplierMapper;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService {

    private final ISupplierMapper supplierMapper;
    private final ISupplierRepo supplierRepo;
    private final IProductRepo productRepo;
    private final IUserRepo userRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmailDomainValidator emailDomainValidator;

    @Override
    public SupplierApplicationDTO applyToSupplierPosition(SupplierApplicationDTO dto) {

        emailDomainValidator.validate(dto.getEmail());

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

        String temporaryPassword = generateTemporaryPassword();
        userRepo.findByEmail(dto.getEmail()).orElseGet(() -> {
            User supplierUser = new User(dto.getEmail(), application.getName(),
                    passwordEncoder.encode(temporaryPassword), Role.USER);
            return userRepo.save(supplierUser);
        });

        emailService.sendSupplierWelcomeEmail(dto.getEmail(), temporaryPassword);

        Optional<Supplier> supplier = supplierRepo.findAll().stream()
                .filter(s -> s.getUser().getEmail().equals(dto.getEmail()))
                .findFirst();

        return supplier.map(supplierMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Supplier record not found after approval for: " + dto.getEmail()));
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++) sb.append(upper.charAt(random.nextInt(upper.length())));
        for (int i = 0; i < 4; i++) sb.append(lower.charAt(random.nextInt(lower.length())));
        for (int i = 0; i < 2; i++) sb.append(digits.charAt(random.nextInt(digits.length())));
        for (int i = 0; i < 2; i++) sb.append(special.charAt(random.nextInt(special.length())));

        List<Character> chars = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) chars.add(c);
        Collections.shuffle(chars, random);

        StringBuilder result = new StringBuilder();
        for (char c : chars) result.append(c);
        return result.toString();
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
