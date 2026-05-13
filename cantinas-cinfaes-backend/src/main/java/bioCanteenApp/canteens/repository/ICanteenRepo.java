package bioCanteenApp.canteens.repository;

import bioCanteenApp.canteens.domain.Canteen;

import java.util.List;
import java.util.Optional;

public interface ICanteenRepo {
    Canteen save(Canteen canteen);
    Optional<Canteen> findById(Long id);
    List<Canteen> findAll();
    Optional<Canteen> findByName(String name);

    List<Canteen> getAllCanteensByVillage(String village);
    List<Canteen>  getAllCanteensByMunicipality(String municipality);
}
