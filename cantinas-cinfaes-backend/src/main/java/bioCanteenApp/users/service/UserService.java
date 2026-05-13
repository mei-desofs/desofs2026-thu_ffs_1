package bioCanteenApp.users.service;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.mapper.IUserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IUserMapper userMapper;
    private final IUserRepo userRepo;

    @Override
    public List<GetUserDTO> getAllUsers() {
        List<User> users = (List<User>) userRepo.findAll();
        return users.stream().map(userMapper::toGetDTO).toList();
    }

    public GetUserDTO getUserById(String email) {
        User user = userRepo.findByEmail(email).iterator().hasNext() ? userRepo.findByEmail(email).iterator().next() : null;
        if (user == null) {
            return null;
        }
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
        User user = userRepo.findByEmail(email).iterator().hasNext() ? userRepo.findByEmail(email).iterator().next() : null;
        if (user == null) {
            return null;
        }
        return userMapper.toGetDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO dto) {

        if (userRepo.findByEmail(dto.getEmail()).iterator().hasNext()) {
            throw new IllegalArgumentException("User Already Exists!");
        }

        dto.setPassword(encodePassword(dto.getPassword()));

        User user = userMapper.toDomain(dto);
        user = userRepo.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public void deleteUser(String email) {
        if (!userRepo.findByEmail(email).iterator().hasNext()) {
            throw new IllegalArgumentException("User Does Not Exist!");
        }

        userRepo.delete(email);
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
