package bioCanteenApp.provisioning.repository;

import bioCanteenApp.provisioning.domain.ProvisioningItem;
import bioCanteenApp.provisioning.domain.ProvisioningType;

import java.util.List;

public interface IProvisioningItemRepo {

    ProvisioningItem save(ProvisioningItem item);

    List<ProvisioningItem> findByMenuAndType(Long menuId, ProvisioningType type);

    void deleteByMenuAndType(Long menuId, ProvisioningType type);
}
