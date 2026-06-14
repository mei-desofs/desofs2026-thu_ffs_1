package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;

public interface IAuthenticationService {
    LoginResponse login(LoginDTO dto);
    LoginResponse refreshToken(String refreshToken);
}
