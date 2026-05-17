package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import bioCanteenApp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final IUserRepo userRepo;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginDTO dto) {
        User user = userRepo.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return null;
        }

        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .user(userMapper.toDTO(user))
                .build();
    }
}