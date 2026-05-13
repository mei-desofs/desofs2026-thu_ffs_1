package bioCanteenApp.suppliers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private String name;
    private String email;
    private String nif;
    private AddressDTO address;
    private String phoneNumber;
    private String certifiedOrganic;
    private Long applicationId;
    private boolean isQuarantined;
}
