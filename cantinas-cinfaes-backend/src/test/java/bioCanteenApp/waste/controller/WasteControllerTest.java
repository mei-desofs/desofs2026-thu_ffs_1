package bioCanteenApp.waste.controller;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.service.IWasteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WasteControllerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IWasteService wasteService;
    private UserRepo userRepository;
    private WasteController controller;

    @BeforeEach
    void setUp() {
        wasteService = mock(IWasteService.class);
        userRepository = mock(UserRepo.class);

        controller = new WasteController(wasteService, userRepository);
    }

    @Test
    void shouldGetDailyWaste() {
        WasteDTO dto = new WasteDTO();

        when(wasteService.getDailyWaste()).thenReturn(dto);

        WasteDTO result = controller.getDailyWaste();

        assertEquals(dto, result);
        verify(wasteService).getDailyWaste();
    }

    @Test
    void shouldGetWeeklyWaste() {
        WasteDTO dto = new WasteDTO();

        when(wasteService.getWeeklyWaste()).thenReturn(dto);

        WasteDTO result = controller.getWeeklyWaste();

        assertEquals(dto, result);
        verify(wasteService).getWeeklyWaste();
    }

    @Test
    void shouldGetMonthlyWaste() {
        WasteDTO dto = new WasteDTO();

        when(wasteService.getMonthlyWaste()).thenReturn(dto);

        WasteDTO result = controller.getMonthlyWaste();

        assertEquals(dto, result);
        verify(wasteService).getMonthlyWaste();
    }

    @Test
    void shouldGetAllWaste() {
        WasteDTO dto = new WasteDTO();

        when(wasteService.getAllWaste()).thenReturn(dto);

        WasteDTO result = controller.getAllWaste();

        assertEquals(dto, result);
        verify(wasteService).getAllWaste();
    }

    @Test
    void shouldGetKPIsForCanteenManager() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        Canteen canteen = createCanteen();
        canteen.setId(1L);

        User user = new User(
                "manager@email.com",
                "Manager",
                "password",
                Role.CANTEEN_MANAGER,
                canteen
        );

        WasteDTO dto = new WasteDTO();

        when(userRepository.findById(1L)).thenReturn(user);
        when(wasteService.getDateRange("monthly"))
                .thenReturn(new LocalDate[]{start, end});
        when(wasteService.aggregateWaste(1L, null, null, start, end))
                .thenReturn(dto);

        WasteDTO result = controller.getKPIs(
                "monthly",
                1L,
                null,
                null,
                null
        );

        assertEquals(dto, result);

        verify(wasteService).aggregateWaste(
                1L,
                null,
                null,
                start,
                end
        );
    }

    @Test
    void shouldGetKPIsForDiningHallManager() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        Canteen canteen = createCanteen();

        DiningHall diningHall = new DiningHall(
                "Main Hall",
                canteen
        );
        diningHall.setId(2L);

        User user = new User(
                "manager@email.com",
                "Manager",
                "password",
                Role.DINING_HALL_MANAGER,
                diningHall
        );

        WasteDTO dto = new WasteDTO();

        when(userRepository.findById(1L)).thenReturn(user);
        when(wasteService.getDateRange("monthly"))
                .thenReturn(new LocalDate[]{start, end});
        when(wasteService.aggregateWaste(null, 2L, null, start, end))
                .thenReturn(dto);

        WasteDTO result = controller.getKPIs(
                "monthly",
                1L,
                null,
                null,
                null
        );

        assertEquals(dto, result);

        verify(wasteService).aggregateWaste(
                null,
                2L,
                null,
                start,
                end
        );
    }

    @Test
    void shouldGetKPIsForNetworkManager() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        User user = new User(
                "network@email.com",
                "Network Manager",
                "password",
                Role.NETWORK_MANAGER
        );

        WasteDTO dto = new WasteDTO();

        when(userRepository.findById(1L)).thenReturn(user);
        when(wasteService.getDateRange("monthly"))
                .thenReturn(new LocalDate[]{start, end});
        when(wasteService.aggregateWaste(10L, 20L, 30L, start, end))
                .thenReturn(dto);

        WasteDTO result = controller.getKPIs(
                "monthly",
                1L,
                10L,
                20L,
                30L
        );

        assertEquals(dto, result);

        verify(wasteService).aggregateWaste(
                10L,
                20L,
                30L,
                start,
                end
        );
    }

    @Test
    void shouldThrowWhenUserNotFoundOnKPIs() {
        when(userRepository.findById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.getKPIs(
                        "monthly",
                        1L,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenRoleIsNotSupported() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        when(userRepository.findById(1L)).thenReturn(user);
        when(wasteService.getDateRange("monthly"))
                .thenReturn(new LocalDate[]{
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31)
                });

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.getKPIs(
                        "monthly",
                        1L,
                        null,
                        null,
                        null
                )
        );
    }

    private Canteen createCanteen() {
        Address address = new Address(
                "Rua Central",
                Municipality.RESENDE,
                Village.ANSIAES,
                "Portugal",
                "4000-111"
        );

        return new Canteen(
                "Main Canteen",
                address,
                100,
                true
        );
    }
}