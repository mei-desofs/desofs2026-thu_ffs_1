package bioCanteenApp.users.service;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.mapper.IUserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserMapper userMapper;
    private final IUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<GetUserDTO> getAllUsers() {
        List<User> users = (List<User>) userRepo.findAll();
        return users.stream().map(userMapper::toGetDTO).toList();
    }

    public GetUserDTO getUserById(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toGetDTO(user);
    }

    @Override
    public UserDTO findUserById(Long id) {
        User dietician = userRepo.findById(id);
        if (dietician == null) {
            throw new RuntimeException("User not found");
        }
        return userMapper.toDTO(dietician);
    }

    @Override
    public GetUserDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toGetDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO dto) {

        userRepo.findByEmail(dto.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("User already exists!");
                });

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        User user = userMapper.toDomain(dto);
        user = userRepo.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public void deleteUser(String email) {
        if (!userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("User not found");
        }
        userRepo.delete(email);
    }
}
