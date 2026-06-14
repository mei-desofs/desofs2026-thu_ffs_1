package bioCanteenApp.waste.service;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;
import bioCanteenApp.waste.mapper.IWasteMapper;
import bioCanteenApp.waste.repository.IWasteRepo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WasteServiceTest {

    private IWasteRepo wasteRepo;
    private IWasteMapper wasteMapper;
    private EntityManager entityManager;

    private WasteService service;

    @BeforeEach
    void setUp() {
        wasteRepo = mock(IWasteRepo.class);
        wasteMapper = mock(IWasteMapper.class);

        entityManager = null;

        service = new WasteService(
                wasteRepo,
                wasteMapper,
                entityManager
        );
    }

    @Test
    void shouldGetDailyWaste() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);
        WasteDTO dto = new WasteDTO();

        when(wasteRepo.findByDate(LocalDate.now()))
                .thenReturn(waste);

        when(wasteMapper.toDTO(waste))
                .thenReturn(dto);

        WasteDTO result = service.getDailyWaste();

        assertEquals(dto, result);

        verify(wasteRepo).findByDate(LocalDate.now());
        verify(wasteMapper).toDTO(waste);
    }

    @Test
    void shouldGetWeeklyWaste() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);
        WasteDTO dto = new WasteDTO();

        when(wasteRepo.findByDateBetween(start, end))
                .thenReturn(waste);

        when(wasteMapper.toDTO(waste))
                .thenReturn(dto);

        WasteDTO result = service.getWeeklyWaste();

        assertEquals(dto, result);

        verify(wasteRepo).findByDateBetween(start, end);
        verify(wasteMapper).toDTO(waste);
    }

    @Test
    void shouldGetMonthlyWaste() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1);

        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);
        WasteDTO dto = new WasteDTO();

        when(wasteRepo.findByDateBetween(start, end))
                .thenReturn(waste);

        when(wasteMapper.toDTO(waste))
                .thenReturn(dto);

        WasteDTO result = service.getMonthlyWaste();

        assertEquals(dto, result);

        verify(wasteRepo).findByDateBetween(start, end);
        verify(wasteMapper).toDTO(waste);
    }

    @Test
    void shouldGetAllWaste() {
        Waste waste = new Waste(100.0, 10.0, 5.0, 80.0);
        WasteDTO dto = new WasteDTO();

        when(wasteRepo.findAggregateAll())
                .thenReturn(waste);

        when(wasteMapper.toDTO(waste))
                .thenReturn(dto);

        WasteDTO result = service.getAllWaste();

        assertEquals(dto, result);

        verify(wasteRepo).findAggregateAll();
        verify(wasteMapper).toDTO(waste);
    }

    @Test
    void shouldAggregateWaste() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        WasteDTO dto = new WasteDTO();

        when(wasteRepo.aggregateWaste(
                1L,
                2L,
                3L,
                start,
                end
        )).thenReturn(dto);

        WasteDTO result = service.aggregateWaste(
                1L,
                2L,
                3L,
                start,
                end
        );

        assertEquals(dto, result);

        verify(wasteRepo).aggregateWaste(
                1L,
                2L,
                3L,
                start,
                end
        );
    }

    @Test
    void shouldGetDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        LocalDate[] range = new LocalDate[]{start, end};

        when(wasteRepo.getDateRange("monthly"))
                .thenReturn(range);

        LocalDate[] result = service.getDateRange("monthly");

        assertArrayEquals(range, result);

        verify(wasteRepo).getDateRange("monthly");
    }
}