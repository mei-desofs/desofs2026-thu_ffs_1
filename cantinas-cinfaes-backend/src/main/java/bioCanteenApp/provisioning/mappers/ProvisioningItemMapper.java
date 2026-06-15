package bioCanteenApp.provisioning.mappers;

import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.provisioning.domain.ProvisioningItem;
import bioCanteenApp.provisioning.dto.ProvisioningItemDTO;
import bioCanteenApp.provisioning.mappers.IProvisioningItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProvisioningItemMapper implements IProvisioningItemMapper {

    private final IMenuRepo menuRepo;
    private final IProductRepo productRepo;

    @Override
    public ProvisioningItemDTO toDTO(ProvisioningItem item) {
        if (item == null) return null;

        return ProvisioningItemDTO.builder()
                .id(item.getId())
                .menuId(item.getMenu().getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .type(item.getType())
                .build();
    }

    @Override
    public ProvisioningItem toDomain(ProvisioningItemDTO dto) {
        if (dto == null) return null;



        return new ProvisioningItem(
                menuRepo.findById(dto.getMenuId()).get(),
                productRepo.findById(dto.getProductId()),
                dto.getQuantity(),
                dto.getType(),
                LocalDateTime.now()
        );
    }
}
