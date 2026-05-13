package bioCanteenApp.canteens.service;

import bioCanteenApp.canteens.dto.CanteenDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ICanteenService {
    CanteenDTO createCanteen(CanteenDTO dto);
    List<CanteenDTO> getAllCanteens();
    CanteenDTO getById(Long id);
    List<CanteenDTO> quarantineCanteensByVillage(String village);
    List<CanteenDTO> unquarantineCanteensByVillage(String village);
    List<CanteenDTO> getByMunicipality(String municipality);
    List<CanteenDTO> getByVillage(String village);
}
