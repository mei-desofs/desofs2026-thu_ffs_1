package bioCanteenApp.suppliers.mapper;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.dto.AddressDTO;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.users.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplierMapper implements ISupplierMapper {

    private AddressDTO mapAddressToDTO(Address address) {
        if (address == null) return null;
        return AddressDTO.builder()
                .street(address.getStreet())
                .municipality(address.getMunicipality() != null ? address.getMunicipality().name() : null)
                .village(address.getVillage() != null ? address.getVillage().name() : null)
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }

    private Address mapAddressToDomain(AddressDTO dto) {
        if (dto == null) return null;
        return new Address(
                dto.getStreet(),
                dto.getMunicipality() != null ? Municipality.valueOf(dto.getMunicipality()) : null,
                dto.getVillage() != null ? Village.valueOf(dto.getVillage()) : null,
                dto.getCountry(),
                dto.getPostalCode()
        );
    }

    @Override
    public SupplierDTO toDTO(Supplier supplier) {
        if (supplier == null) return null;

        SupplierDTO.SupplierDTOBuilder builder = SupplierDTO.builder()
                .name(supplier.getUser() != null ? supplier.getUser().getName() : null)
                .email(supplier.getUser() != null ? supplier.getUser().getEmail() : null)
                .nif(supplier.getNif())
                .address(mapAddressToDTO(supplier.getAddress()))
                .phoneNumber(supplier.getPhoneNumber())
                .applicationId(supplier.getApplicationId() != null ? supplier.getApplicationId().getId() : null)
                .isQuarantined(supplier.isQuarantined());

        if (supplier.getCertifiedOrganic() != null) {
            builder.certifiedOrganic(Base64.getEncoder().encodeToString(supplier.getCertifiedOrganic()));
        }

        return builder.build();
    }

    @Override
    public Supplier toDomain(User user, SupplierDTO dto, SupplierApplicationDTO applicationDTO) {
        if (dto == null || applicationDTO == null) return null;

        SupplierApplication application = toDomain(applicationDTO);

        byte[] certifiedOrganicBytes = null;
        if (dto.getCertifiedOrganic() != null) {
            certifiedOrganicBytes = Base64.getDecoder().decode(dto.getCertifiedOrganic());
        }

        return new Supplier(
                user,
                dto.getNif(),
                mapAddressToDomain(dto.getAddress()),
                dto.getPhoneNumber(),
                certifiedOrganicBytes,
                application
        );
    }

    @Override
    public SupplierApplicationDTO toDTO(SupplierApplication supplierApplication) {
        if (supplierApplication == null) return null;

        List<SupplierApplicationDTO.SupplierCapacityDTO> capacitiesDTO =
                supplierApplication.getSupplierCapacity().stream()
                        .map(c -> SupplierApplicationDTO.SupplierCapacityDTO.builder()
                                .productName(c.getProductName())
                                .startDate(c.getStartDate())
                                .endDate(c.getEndDate())
                                .quantity(c.getQuantity())
                                .build())
                        .collect(Collectors.toList());

        SupplierApplicationDTO.SupplierApplicationDTOBuilder builder = SupplierApplicationDTO.builder()
                .name(supplierApplication.getName())
                .email(supplierApplication.getEmail())
                .address(mapAddressToDTO(supplierApplication.getAddress()))
                .nif(supplierApplication.getNif())
                .applicationDate(supplierApplication.getApplicationDate())
                .status(supplierApplication.getStatus().name())
                .interviewStatus(supplierApplication.getInterviewStatus() != null
                        ? supplierApplication.getInterviewStatus().name()
                        : null)
                .supplierCapacity(capacitiesDTO);

        if (supplierApplication.getBioCertificate() != null) {
            builder.bioCertificate(Base64.getEncoder().encodeToString(supplierApplication.getBioCertificate()));
        }
        return builder.build();
    }

    public SupplierApplication toDomain(SupplierApplicationDTO dto) {
        List<SupplierCapacity> capacities = (dto.getSupplierCapacity() != null)
                ? dto.getSupplierCapacity().stream()
                .map(c -> new SupplierCapacity(
                        c.getProductName(),
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getQuantity()
                ))
                .collect(Collectors.toList())
                : new ArrayList<>();

        byte[] bioCertificateBytes = null;
        if (dto.getBioCertificate() != null) {
            bioCertificateBytes = Base64.getDecoder().decode(dto.getBioCertificate());
        }

        SupplierApplication app = new SupplierApplication(
                dto.getName(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                mapAddressToDomain(dto.getAddress()),
                bioCertificateBytes,
                dto.getNif(),
                capacities,
                dto.getApplicationDate() != null ? dto.getApplicationDate() : LocalDate.now()
        );
        app.setStatus(SupplierApplicationStatus.PENDING);
        app.setInterviewStatus(InterviewStatus.TO_BE_DONE);

        return app;
    }

}
