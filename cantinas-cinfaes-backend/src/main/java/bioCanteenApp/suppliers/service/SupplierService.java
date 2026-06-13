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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Transactional
    public SupplierApplicationDTO applyToSupplierPosition(SupplierApplicationDTO dto, MultipartFile certificate) {
        // Validação de Email (existente)
        emailDomainValidator.validate(dto.getEmail());

        // REQ 3.3: Validate BIO Certificate (PDF & Max 5MB)
        if (certificate == null || certificate.isEmpty()) {
            throw new IllegalArgumentException("BIO Certificate file is required.");
        }
        if (!"application/pdf".equalsIgnoreCase(certificate.getContentType())) {
            throw new IllegalArgumentException("BIO Certificate must be a valid PDF file.");
        }
        if (certificate.getSize() > 5 * 1024 * 1024) { // 5MB in bytes
            throw new IllegalArgumentException("BIO Certificate size cannot exceed 5MB.");
        }

        SupplierApplication application = supplierMapper.toDomain(dto);

        try {
            application.setBioCertificate(certificate.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error reading the BIO Certificate file", e);
        }

        // Atribuição de Capacidades Produtivas
        List<SupplierCapacity> capacities = dto.getSupplierCapacity().stream()
                .map(c -> new SupplierCapacity(c.getProductName(), c.getStartDate(), c.getEndDate(), c.getQuantity()))
                .collect(Collectors.toList());
        application.setSupplierCapacity(capacities);

        // Define estado inicial
        application.setStatus(SupplierApplicationStatus.PENDING);
        application.setApplicationDate(java.time.LocalDate.now());

        supplierRepo.save(application);
        return supplierMapper.toDTO(application);
    }

    @Override
    @Transactional
    public SupplierDTO approveSupplier(Long applicationId) {
        SupplierApplication application = supplierRepo.findApplicationById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // REQ 4.2: Only candidates that passed interview can be approved
        if (!"APPROVED".equalsIgnoreCase(application.getInterviewStatus().toString())) {
            throw new RuntimeException("Application cannot be approved: Candidate has not passed the interview phase.");
        }

        application.setStatus(SupplierApplicationStatus.APPROVED);
        supplierRepo.save(application);

        // REQ 4.3: Generate setup Token/Password
        String setupToken = UUID.randomUUID().toString(); // Token linkativo de 24h
        String temporaryPassword = UUID.randomUUID().toString(); // Fallback genérico para a Entity

        User supplierUser = userRepo.findByEmail(application.getEmail()).orElseGet(() -> {
            User newUser = new User(application.getEmail(), application.getName(),
                    passwordEncoder.encode(temporaryPassword), Role.USER);
            return userRepo.save(newUser);
        });

        // Cria a entidade de Supplier
        Supplier supplier = new Supplier();
        supplier.setUser(supplierUser);
        supplier.setNif(application.getNif().toString());
        supplier.setApplicationId(application);

        supplier.setAddress(application.getAddress());
        supplier.setPhoneNumber(application.getPhoneNumber());
        supplier.setCertifiedOrganic(application.getBioCertificate());
        // Assumimos que o Mapper copia o address e o phone corretamente

        supplierRepo.save(supplier);

        // REQ 4.3: Send email with setup link (ativo por 24h)
        String setupLink = "http://localhost:8080/api/auth/set-password?token=" + setupToken;
        // No teu EmailService, ajusta este método para receber o Link em vez da pass pura.
        emailService.sendSupplierWelcomeEmail(application.getEmail(), setupLink);

        return supplierMapper.toDTO(supplier);
    }

    @Override
    @Transactional
    public SupplierDTO rejectSupplier(Long applicationId, String reason) {
        SupplierApplication application = supplierRepo.findApplicationById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(SupplierApplicationStatus.REJECTED);
        supplierRepo.save(application);

        // REQ 4.4: Enviar email com a razão da rejeição
        // Garante que o teu EmailService tem um método similar a este
        emailService.sendRejectionEmail(application.getEmail(), reason);

        // Retornar um SupplierDTO representativo ou vazio dependendo da arquitetura
        SupplierDTO response = new SupplierDTO();
        response.setEmail(application.getEmail());
        response.setName(application.getName());
        return response;
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

    @Override
    public byte[] getBioCertificate(Long applicationId) {
        SupplierApplication app = supplierRepo.findApplicationById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getBioCertificate() == null) {
            throw new RuntimeException("No certificate uploaded for this application.");
        }

        return app.getBioCertificate();
    }
}
