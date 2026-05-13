package bioCanteenApp.suppliers.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierApplicationDTO {

    private Long id;   // ✅ ADD THIS

    private String name;
    private String email;
    private String phoneNumber;

    private AddressDTO address;

    private Long nif;

    private String bioCertificate;

    private LocalDate applicationDate;

    private String status;
    private String interviewStatus;

    private List<SupplierCapacityDTO> supplierCapacity;

    @Getter
    @Setter
    @Builder
    public static class SupplierCapacityDTO {
        private String productName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double quantity;
    }
}
