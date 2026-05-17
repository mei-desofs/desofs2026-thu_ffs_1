package bioCanteenApp.canteens.service;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.mappers.ICanteenMapper;
import bioCanteenApp.canteens.repository.ICanteenRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.suppliers.domain.Supplier;
import bioCanteenApp.suppliers.domain.SupplierApplication;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.utils.exceptions.CanteenExceptions.CanteenAlreadyExists;
import bioCanteenApp.utils.exceptions.CanteenExceptions.CanteenNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanteenServiceTest {

    @Mock
    private ICanteenRepo repo;

    @Mock
    private ICanteenMapper mapper;

    @Mock
    private ISupplierRepo supplierRepo;

    @Mock
    private IProductRepo productRepo;

    @InjectMocks
    private CanteenService service;

    @Test
    void shouldCreateCanteen() {
        CanteenDTO dto = null;
        CanteenDTO savedDto = null;

        Canteen canteen = createCanteen("ISEP Canteen");
        Canteen savedCanteen = createCanteen("ISEP Canteen");

        when(mapper.toDomain(dto)).thenReturn(canteen);
        when(repo.findByName("ISEP Canteen")).thenReturn(Optional.empty());
        when(repo.save(canteen)).thenReturn(savedCanteen);
        when(mapper.toDTO(savedCanteen)).thenReturn(savedDto);

        CanteenDTO result = service.createCanteen(dto);

        assertEquals(savedDto, result);
        verify(repo).findByName("ISEP Canteen");
        verify(repo).save(canteen);
        verify(mapper).toDTO(savedCanteen);
    }

    @Test
    void shouldThrowWhenCanteenAlreadyExists() {
        CanteenDTO dto = null;
        Canteen canteen = createCanteen("ISEP Canteen");

        when(mapper.toDomain(dto)).thenReturn(canteen);
        when(repo.findByName("ISEP Canteen")).thenReturn(Optional.of(canteen));

        assertThrows(CanteenAlreadyExists.class, () -> service.createCanteen(dto));

        verify(repo, never()).save(any());
    }

    @Test
    void shouldGetAllCanteens() {
        Canteen canteen1 = createCanteen("Canteen 1");
        Canteen canteen2 = createCanteen("Canteen 2");

        when(repo.findAll()).thenReturn(List.of(canteen1, canteen2));
        when(mapper.toDTO(canteen1)).thenReturn(null);
        when(mapper.toDTO(canteen2)).thenReturn(null);

        List<CanteenDTO> result = service.getAllCanteens();

        assertEquals(2, result.size());
        verify(mapper).toDTO(canteen1);
        verify(mapper).toDTO(canteen2);
    }

    @Test
    void shouldGetCanteenById() {
        Canteen canteen = createCanteen("ISEP Canteen");

        when(repo.findById(1L)).thenReturn(Optional.of(canteen));
        when(mapper.toDTO(canteen)).thenReturn(null);

        CanteenDTO result = service.getById(1L);

        assertNull(result);
        verify(mapper).toDTO(canteen);
    }

    @Test
    void shouldThrowWhenCanteenNotFoundById() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CanteenNotFound.class, () -> service.getById(1L));
    }

    @Test
    void shouldQuarantineCanteensByVillage() {
        Canteen canteen = createCanteen("ISEP Canteen");
        Supplier supplier = createSupplier();
        ProductBatch batch = createProductBatch(supplier);

        when(repo.getAllCanteensByVillage("ANSIAES")).thenReturn(List.of(canteen));
        when(supplierRepo.getAllSuppliersByVillage("ANSIAES")).thenReturn(List.of(supplier));
        when(productRepo.findProductsBySupplier(supplier)).thenReturn(List.of(batch));
        when(mapper.toDTO(canteen)).thenReturn(null);

        List<CanteenDTO> result = service.quarantineCanteensByVillage("ansiaes");

        assertTrue(canteen.getIsQuarantined());
        assertTrue(supplier.isQuarantined());
        assertTrue(batch.isQuarantined());
        assertEquals(1, result.size());
    }

    @Test
    void shouldUnquarantineCanteensByVillage() {
        Canteen canteen = createCanteen("ISEP Canteen");
        canteen.setIsQuarantined(true);

        Supplier supplier = createSupplier();
        supplier.setQuarantined(true);

        ProductBatch batch = createProductBatch(supplier);
        batch.setQuarantined(true);

        when(repo.getAllCanteensByVillage("ANSIAES")).thenReturn(List.of(canteen));
        when(supplierRepo.getAllSuppliersByVillage("ANSIAES")).thenReturn(List.of(supplier));
        when(productRepo.findProductsBySupplier(supplier)).thenReturn(List.of(batch));
        when(mapper.toDTO(canteen)).thenReturn(null);

        List<CanteenDTO> result = service.unquarantineCanteensByVillage("ansiaes");

        assertFalse(canteen.getIsQuarantined());
        assertFalse(supplier.isQuarantined());
        assertFalse(batch.isQuarantined());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetByMunicipality() {
        Canteen canteen = createCanteen("ISEP Canteen");

        when(repo.getAllCanteensByMunicipality("RESENDE")).thenReturn(List.of(canteen));
        when(mapper.toDTO(canteen)).thenReturn(null);

        List<CanteenDTO> result = service.getByMunicipality("RESENDE");

        assertEquals(1, result.size());
        verify(mapper).toDTO(canteen);
    }

    @Test
    void shouldGetByVillage() {
        Canteen canteen = createCanteen("ISEP Canteen");

        when(repo.getAllCanteensByVillage("ANSIAES")).thenReturn(List.of(canteen));
        when(mapper.toDTO(canteen)).thenReturn(null);

        List<CanteenDTO> result = service.getByVillage("ANSIAES");

        assertEquals(1, result.size());
        verify(mapper).toDTO(canteen);
    }

    private Canteen createCanteen(String name) {
        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.ANSIAES,
                "Portugal",
                "4000-111"
        );

        return new Canteen(
                name,
                address,
                300,
                true
        );
    }

    private Supplier createSupplier() {
        Address address = new Address(
                "Rua Supplier",
                Municipality.RESENDE,
                Village.ANSIAES,
                "Portugal",
                "4000-222"
        );

        User user = new User(
                "supplier@email.com",
                "Supplier",
                "password",
                Role.USER
        );

        SupplierApplication application = new SupplierApplication(
                "Supplier",
                "supplier@email.com",
                "912345678",
                address,
                new byte[]{1, 2, 3},
                123456789L,
                List.of(),
                LocalDate.of(2026, 5, 14)
        );

        return new Supplier(
                user,
                "123456789",
                address,
                "912345678",
                new byte[]{1, 2, 3},
                application
        );
    }

    private ProductBatch createProductBatch(Supplier supplier) {
        Product product = new Product(
                "Rice",
                "kg",
                365,
                List.of(Season.SPRING),
                List.of(Allergen.GLUTEN)
        );

        return new ProductBatch(
                product,
                10.0,
                LocalDate.of(2026, 5, 14),
                true,
                supplier
        );
    }
}