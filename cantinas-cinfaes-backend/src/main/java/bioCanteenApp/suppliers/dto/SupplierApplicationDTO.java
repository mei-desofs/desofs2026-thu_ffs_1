package bioCanteenApp.suppliers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierApplicationDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Address is required")
    @Valid // Valida também os campos dentro de AddressDTO
    private AddressDTO address;

    @NotBlank(message = "NIF is required")
    @Pattern(regexp = "^\\d{9}$", message = "NIF must be exactly 9 digits")
    private String nif;

    private String bioCertificatePath;

    private String bioCertificate;

    private LocalDate applicationDate;

    private String status;
    private String interviewStatus;

    @NotEmpty(message = "Productive capability is required")
    @Valid
    private List<SupplierCapacityDTO> supplierCapacity;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierCapacityDTO {
        @NotBlank(message = "Product name is required")
        private String productName;
        @NotNull(message = "Start date is required")
        private LocalDate startDate;
        @NotNull(message = "End date is required")
        private LocalDate endDate;
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Double quantity;
    }
}