package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.exception.InvalidCredentialsException;
import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import bioCanteenApp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final IUserRepo userRepo;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    @Override
    public LoginResponse login(LoginDTO dto) {

        loginAttemptService.checkNotLocked(dto.getEmail());

        User user = userRepo.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.registerFailure(dto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        loginAttemptService.resetAttempts(dto.getEmail());

        checkAndAlertNewDevice(user, dto.getDeviceId());
        user.setLastDeviceId(dto.getDeviceId());
        userRepo.save(user);

        // emailService.alertIfNewDevice(user, dto.getDeviceId());


        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .user(userMapper.toDTO(user))
                .build();
    }

    private void checkAndAlertNewDevice(User user, String incomingDeviceId) {

        if (incomingDeviceId == null) return;

        String lastDevice = user.getLastDeviceId();

        if (lastDevice != null && !lastDevice.equals(incomingDeviceId)) {
            emailService.sendNewDeviceAlert(
                    user.getEmail(),
                    incomingDeviceId,
                    LocalDateTime.now()
            );
        }
    }
}