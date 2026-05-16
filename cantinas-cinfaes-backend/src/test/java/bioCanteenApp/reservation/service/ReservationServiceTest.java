package bioCanteenApp.reservation.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.menu.domain.MenuEntry;
import bioCanteenApp.menu.domain.MenuEntryDish;
import bioCanteenApp.menu.domain.MenuStatus;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.reservation.domain.Reservation;
import bioCanteenApp.reservation.domain.ReservationStatus;
import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.mappers.ReservationMapper;
import bioCanteenApp.reservation.repository.IReservationRepo;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.utils.exceptions.ReservationExceptions.ReservationAlreadyExists;
import bioCanteenApp.utils.exceptions.ReservationExceptions.ReservationNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IReservationRepo repo;
    private IMenuRepo menuRepo;
    private ReservationMapper mapper;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        repo = mock(IReservationRepo.class);
        menuRepo = mock(IMenuRepo.class);
        mapper = mock(ReservationMapper.class);

        service = new ReservationService(repo, menuRepo, mapper);
    }

    @Test
    void shouldCreateReservation() {
        ReservationDTO dto = createReservationDTO(1L, 10L);
        Reservation reservation = createReservation(1L, 10L);
        Reservation saved = createReservation(1L, 10L);
        ReservationDTO savedDto = createReservationDTO(1L, 10L);

        when(mapper.toDomain(dto)).thenReturn(reservation);
        when(repo.findByUserId(1L)).thenReturn(List.of());
        when(menuRepo.findByMenuEntryDishId(10L)).thenReturn(Optional.empty());
        when(repo.save(reservation)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(savedDto);

        ReservationDTO result = service.createReservation(dto);

        assertEquals(savedDto, result);

        verify(repo).save(reservation);
        verify(mapper).toDTO(saved);
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        ReservationDTO dto = createReservationDTO(null, 10L);
        Reservation reservation = createReservation(1L, 10L);

        when(mapper.toDomain(dto)).thenReturn(reservation);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createReservation(dto)
        );

        verify(repo, never()).save(any());
    }

    @Test
    void shouldThrowWhenSameMenuEntryDishAlreadyExists() {
        ReservationDTO dto = createReservationDTO(1L, 10L);

        Reservation newReservation = createReservation(1L, 10L);
        Reservation existingReservation = createReservation(1L, 10L);

        when(mapper.toDomain(dto)).thenReturn(newReservation);
        when(repo.findByUserId(1L)).thenReturn(List.of(existingReservation));

        assertThrows(
                ReservationAlreadyExists.class,
                () -> service.createReservation(dto)
        );

        verify(repo, never()).save(any());
    }

    @Test
    void shouldThrowWhenSameMenuAlreadyHasReservation() {
        ReservationDTO dto = createReservationDTO(1L, 10L);

        Reservation newReservation = createReservation(1L, 10L);
        Reservation existingReservation = createReservation(1L, 20L);

        Menu menu = new Menu(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 17),
                MenuStatus.GENERATED
        );
        menu.setId(100L);

        when(mapper.toDomain(dto)).thenReturn(newReservation);
        when(repo.findByUserId(1L)).thenReturn(List.of(existingReservation));
        when(menuRepo.findByMenuEntryDishId(10L)).thenReturn(Optional.of(menu));
        when(menuRepo.findByMenuEntryDishId(20L)).thenReturn(Optional.of(menu));

        assertThrows(
                ReservationAlreadyExists.class,
                () -> service.createReservation(dto)
        );

        verify(repo, never()).save(any());
    }

    @Test
    void shouldThrowWhenSameReservationDateTimeAlreadyExists() {
        ReservationDTO dto = createReservationDTO(1L, 10L);

        Reservation newReservation = createReservation(1L, 10L);
        Reservation existingReservation = createReservation(1L, 20L);

        LocalDateTime dateTime = LocalDateTime.of(2026, 5, 14, 12, 30);

        newReservation.setReservationDateTime(dateTime);
        existingReservation.setReservationDateTime(dateTime);

        when(mapper.toDomain(dto)).thenReturn(newReservation);
        when(repo.findByUserId(1L)).thenReturn(List.of(existingReservation));
        when(menuRepo.findByMenuEntryDishId(10L)).thenReturn(Optional.empty());

        assertThrows(
                ReservationAlreadyExists.class,
                () -> service.createReservation(dto)
        );

        verify(repo, never()).save(any());
    }

    @Test
    void shouldGetAllReservations() {
        Reservation reservation1 = createReservation(1L, 10L);
        Reservation reservation2 = createReservation(2L, 20L);

        ReservationDTO dto1 = createReservationDTO(1L, 10L);
        ReservationDTO dto2 = createReservationDTO(2L, 20L);

        when(repo.findAll()).thenReturn(List.of(reservation1, reservation2));
        when(mapper.toDTO(reservation1)).thenReturn(dto1);
        when(mapper.toDTO(reservation2)).thenReturn(dto2);

        List<ReservationDTO> result = service.getAllReservations();

        assertEquals(List.of(dto1, dto2), result);

        verify(repo).findAll();
        verify(mapper).toDTO(reservation1);
        verify(mapper).toDTO(reservation2);
    }

    @Test
    void shouldGetById() {
        Reservation reservation = createReservation(1L, 10L);
        ReservationDTO dto = createReservationDTO(1L, 10L);

        when(repo.findById(1L)).thenReturn(Optional.of(reservation));
        when(mapper.toDTO(reservation)).thenReturn(dto);

        ReservationDTO result = service.getById(1L);

        assertEquals(dto, result);

        verify(repo).findById(1L);
        verify(mapper).toDTO(reservation);
    }

    @Test
    void shouldThrowWhenReservationNotFoundById() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ReservationNotFound.class,
                () -> service.getById(1L)
        );

        verify(mapper, never()).toDTO(any());
    }

    @Test
    void shouldGetByUserId() {
        Reservation reservation1 = createReservation(1L, 10L);
        Reservation reservation2 = createReservation(1L, 20L);

        ReservationDTO dto1 = createReservationDTO(1L, 10L);
        ReservationDTO dto2 = createReservationDTO(1L, 20L);

        when(repo.findByUserId(1L)).thenReturn(List.of(reservation1, reservation2));
        when(mapper.toDTO(reservation1)).thenReturn(dto1);
        when(mapper.toDTO(reservation2)).thenReturn(dto2);

        List<ReservationDTO> result = service.getByUserId(1L);

        assertEquals(List.of(dto1, dto2), result);

        verify(repo).findByUserId(1L);
        verify(mapper).toDTO(reservation1);
        verify(mapper).toDTO(reservation2);
    }

    private ReservationDTO createReservationDTO(Long userId, Long menuEntryDishId) {
        ReservationDTO dto = new ReservationDTO();
        dto.setUserId(userId);
        dto.setMenuEntryDishId(menuEntryDishId);
        dto.setReservationDateTime(LocalDateTime.of(2026, 5, 14, 12, 30));
        dto.setStatus(ReservationStatus.PENDING);
        return dto;
    }

    private Reservation createReservation(Long userId, Long menuEntryDishId) {
        User user = new User(
                "user" + userId + "@email.com",
                "User " + userId,
                "password"
        );
        user.setId(userId);

        MenuEntry menuEntry = new MenuEntry();

        Dish dish = new Dish();

        MenuEntryDish menuEntryDish = new MenuEntryDish(
                menuEntry,
                dish
        );
        menuEntryDish.setId(menuEntryDishId);

        return new Reservation(
                user,
                menuEntryDish,
                LocalDateTime.of(2026, 5, 14, 12, 30),
                ReservationStatus.PENDING
        );
    }
}