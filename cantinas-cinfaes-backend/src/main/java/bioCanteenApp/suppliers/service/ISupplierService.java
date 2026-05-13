package bioCanteenApp.suppliers.service;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.suppliers.dto.SupplierApplicationDTO;
import bioCanteenApp.suppliers.dto.SupplierDTO;

import java.util.List;
import java.util.Map;

public interface ISupplierService {

    SupplierApplicationDTO applyToSupplierPosition(SupplierApplicationDTO resource);

    SupplierDTO approveSupplier(SupplierDTO dto);

    SupplierDTO rejectSupplier(SupplierDTO dto);

    Map<String, Long> getSupplierStats();

    List<SupplierDTO> findAllSuppliers();

    List<SupplierDTO> findAllSuppliersByOrderByProduct(Long id);

    List<SupplierApplicationDTO> findAllApplications();

    SupplierDTO quarantineSupplier(SupplierDTO dto);

    SupplierDTO unquarantineSupplier(SupplierDTO dto);

    List<SupplierDTO> getSuppliersByName(String name);

    List<SupplierDTO> getSuppliersByVillage(String village);

    List<SupplierDTO> getSuppliersByMunicipality(String municipality);

}
