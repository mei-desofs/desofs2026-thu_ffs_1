package bioCanteenApp.users.service;

import bioCanteenApp.email.validator.EmailDomainValidator;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.mapper.IUserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private IUserMapper userMapper;
    private IUserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private EmailDomainValidator emailDomainValidator;

    private UserService service;

    @BeforeEach
    void setUp() {
        userMapper = mock(IUserMapper.class);
        userRepo = mock(IUserRepo.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailDomainValidator = mock(EmailDomainValidator.class);

        service = new UserService(
                userMapper,
                userRepo,
                passwordEncoder,
                emailDomainValidator
        );
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User(
                "user1@email.com",
                "User 1",
                "password",
                Role.USER
        );

        User user2 = new User(
                "user2@email.com",
                "User 2",
                "password",
                Role.USER
        );

        GetUserDTO dto1 = mock(GetUserDTO.class);
        GetUserDTO dto2 = mock(GetUserDTO.class);

        when(userRepo.findAll())
                .thenReturn(List.of(user1, user2));

        when(userMapper.toGetDTO(user1))
                .thenReturn(dto1);

        when(userMapper.toGetDTO(user2))
                .thenReturn(dto2);

        List<GetUserDTO> result = service.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(List.of(dto1, dto2), result);
    }

    @Test
    void shouldGetUserById() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        GetUserDTO dto = mock(GetUserDTO.class);

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toGetDTO(user))
                .thenReturn(dto);

        GetUserDTO result =
                service.getUserById("user@email.com");

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {
        when(userRepo.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.getUserById("missing@email.com")
        );
    }

    @Test
    void shouldFindUserById() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        UserDTO dto = mock(UserDTO.class);

        when(userRepo.findById(1L))
                .thenReturn(user);

        when(userMapper.toDTO(user))
                .thenReturn(dto);

        UserDTO result = service.findUserById(1L);

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowWhenFindUserByIdReturnsNull() {
        when(userRepo.findById(1L))
                .thenReturn(null);

        assertThrows(
                RuntimeException.class,
                () -> service.findUserById(1L)
        );
    }

    @Test
    void shouldGetUserByEmail() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        GetUserDTO dto = mock(GetUserDTO.class);

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toGetDTO(user))
                .thenReturn(dto);

        GetUserDTO result =
                service.getUserByEmail("user@email.com");

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowWhenGetUserByEmailFails() {
        when(userRepo.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.getUserByEmail("missing@email.com")
        );
    }

    @Test
    void shouldCreateUser() {
        UserDTO dto = mock(UserDTO.class);
        UserDTO resultDto = mock(UserDTO.class);

        User user = new User(
                "user@email.com",
                "User",
                "encodedPassword",
                Role.USER
        );

        when(dto.getEmail())
                .thenReturn("user@email.com");

        when(dto.getPassword())
                .thenReturn("password");

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.empty());

        when(userMapper.toDomain(dto))
                .thenReturn(user);

        when(userRepo.save(user))
                .thenReturn(user);

        when(userMapper.toDTO(user))
                .thenReturn(resultDto);

        UserDTO result = service.createUser(dto);

        assertEquals(resultDto, result);

        verify(emailDomainValidator)
                .validate("user@email.com");

        verify(dto)
                .setPassword("encodedPassword");

        verify(userRepo)
                .save(user);
    }

    @Test
    void shouldThrowWhenUserAlreadyExists() {
        UserDTO dto = mock(UserDTO.class);

        User existingUser = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        when(dto.getEmail())
                .thenReturn("user@email.com");

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(dto)
        );

        verify(userRepo, never()).save(any());
    }

    @Test
    void shouldDeleteUser() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        service.deleteUser("user@email.com");

        verify(userRepo).delete("user@email.com");
    }

    @Test
    void shouldThrowWhenDeleteUserNotFound() {
        when(userRepo.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.deleteUser("missing@email.com")
        );

        verify(userRepo, never()).delete(any());
    }
}