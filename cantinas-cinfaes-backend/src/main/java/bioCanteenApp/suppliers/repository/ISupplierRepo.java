package bioCanteenApp.suppliers.repository;

import bioCanteenApp.products.domain.Product;
import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.domain.SupplierApplication;
import bioCanteenApp.suppliers.domain.SupplierApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface ISupplierRepo {

    Optional<SupplierApplication> findApplicationById(Long id);

    Supplier save(Supplier supplier);

    SupplierApplication save(SupplierApplication supplierApplication);

    Optional<SupplierApplication> findByEmail(String email);

    long countTotalSuppliers();

    long countApplicationsByStatus(SupplierApplicationStatus supplierApplicationStatus);

    List<Supplier> findAll();

    Supplier findBySupplierEmail(String email);

    List<Supplier> findAllSuppliersByOrderByProduct(Product product);

    List<SupplierApplication> findAllApplications();

    List<Supplier> getAllSuppliersByVillage(String village);

    List<Supplier> findSuppliersByName(String name);

    List<Supplier> findSuppliersByVillage(String village);

    List<Supplier> findSuppliersByMunicipality(String municipality);

    Supplier findById(Long id);

    List<SupplierApplication> findApprovedApplicationsFromNonQuarantinedSuppliers();


}
