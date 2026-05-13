package bioCanteenApp.canteens.mappers;

import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.address.Address;
import bioCanteenApp.suppliers.dto.AddressDTO;
import org.springframework.stereotype.Component;

@Component
public class CanteenMapper implements ICanteenMapper {

    @Override
    public CanteenDTO toDTO(Canteen canteen) {
        if (canteen == null) return null;

        return CanteenDTO.builder()
                .name(canteen.getName())
                .location(toAddressDTO(canteen.getLocation()))
                .capacity(canteen.getCapacity())
                .canCookDishes(canteen.getCanCookDishes())
                .isQuarantine(canteen.getIsQuarantined())
                .build();
    }

    @Override
    public Canteen toDomain(CanteenDTO dto) {
        if (dto == null) return null;

        Canteen canteen = new Canteen(
                dto.getName(),
                toAddress(dto.getLocation()),
                dto.getCapacity(),
                dto.getCanCookDishes()
        );

        canteen.setIsQuarantined(dto.getIsQuarantine());

        return canteen;
    }

    private AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;

        return AddressDTO.builder()
                .street(address.getStreet())
                .municipality(
                        address.getMunicipality() != null
                                ? address.getMunicipality().name()
                                : null
                )
                .village(
                        address.getVillage() != null
                                ? address.getVillage().name()
                                : null
                )
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }

    private Address toAddress(AddressDTO dto) {
        if (dto == null) return null;

        return new Address(
                dto.getStreet(),
                dto.getMunicipality() != null
                        ? Enum.valueOf(
                        Municipality.class,
                        dto.getMunicipality()
                )
                        : null,
                dto.getVillage() != null
                        ? Enum.valueOf(
                        Village.class,
                        dto.getVillage()
                )
                        : null,
                dto.getCountry(),
                dto.getPostalCode()
        );
    }
}
