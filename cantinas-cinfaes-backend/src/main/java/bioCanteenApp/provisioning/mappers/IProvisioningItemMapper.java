package bioCanteenApp.provisioning.mappers;

import bioCanteenApp.provisioning.domain.ProvisioningItem;
import bioCanteenApp.provisioning.dto.ProvisioningItemDTO;

public interface IProvisioningItemMapper {
    ProvisioningItemDTO toDTO(ProvisioningItem item);
    ProvisioningItem toDomain(ProvisioningItemDTO dto);
}
