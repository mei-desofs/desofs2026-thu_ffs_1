package bioCanteenApp.provisioning.dto;

import bioCanteenApp.provisioning.domain.ProvisioningType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvisioningItemDTO {

    private Long id;
    private Long menuId;
    private Long productId;
    private Double quantity;
    private ProvisioningType type;
}
