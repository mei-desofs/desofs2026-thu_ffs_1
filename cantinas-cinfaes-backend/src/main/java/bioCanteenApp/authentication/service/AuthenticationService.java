package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IUserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public LoginResponse login(LoginDTO dto) {
        User user = userRepo.findByEmail(dto.getEmail()).iterator().hasNext()
                  ? userRepo.findByEmail(dto.getEmail()).iterator().next() : null;

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(dto.getPassword(), decodePassword(user.getPassword()))) {
            return null;
        }

        return LoginResponse.builder()
                .token("JWT Token")
                .tokenType("Bearer")
                .user(userMapper.toDTO(user))
                .build();
    }

    private String decodePassword(String encodedPassword) {
        return passwordEncoder.encode(encodedPassword);
    }
}
