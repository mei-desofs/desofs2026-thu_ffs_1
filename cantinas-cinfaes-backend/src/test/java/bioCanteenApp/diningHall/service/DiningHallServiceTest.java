package bioCanteenApp.diningHall.service;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.mappers.IDiningHallMapper;
import bioCanteenApp.diningHall.repository.IDiningHallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiningHallServiceTest {

    @Mock
    private IDiningHallRepository repo;

    @Mock
    private IDiningHallMapper mapper;

    @InjectMocks
    private DiningHallService service;

    @Test
    void shouldGetAllDiningHalls() {

        Canteen canteen = new Canteen(
                "ISEP Canteen",
                new Address(
                        "Rua Central",
                        Municipality.RESENDE,
                        Village.ANSIAES,
                        "Portugal",
                        "4000-111"
                ),
                300,
                true
        );

        DiningHall diningHall1 = new DiningHall(
                "Main Hall",
                canteen
        );

        DiningHall diningHall2 = new DiningHall(
                "Secondary Hall",
                canteen
        );

        when(repo.findAll()).thenReturn(List.of(diningHall1, diningHall2));

        when(mapper.toDTO(diningHall1)).thenReturn(null);
        when(mapper.toDTO(diningHall2)).thenReturn(null);

        List<DiningHallDTO> result = service.getAllDiningHall();

        assertEquals(2, result.size());

        verify(mapper).toDTO(diningHall1);
        verify(mapper).toDTO(diningHall2);
    }
}