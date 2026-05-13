package bioCanteenApp.suppliers.mapper;

import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.domain.SupplierApplication;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;
import bioCanteenApp.users.domain.User;

public interface ISupplierMapper {

    SupplierDTO toDTO(Supplier supplier);
    Supplier toDomain(User user, SupplierDTO dto, SupplierApplicationDTO applicationDTO);

    SupplierApplicationDTO toDTO(SupplierApplication supplierApplication);

    SupplierApplication toDomain(SupplierApplicationDTO dto);
}
