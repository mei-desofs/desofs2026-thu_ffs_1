package bioCanteenApp.security;

import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.exception.InvalidCredentialsException;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final IUserRepo userRepo;
    private final UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // Secret key is loaded at startup either from configuration (recommended) or generated as a fallback
    private Key secretKey;

    // Base64-encoded secret supplied via configuration. Example: jwt.secret=BASE64_KEY
    @Value("${jwt.secret:}")
    private String jwtSecret;

    // Default expiration is 20 minutos
    private static final long EXPIRATION_TIME = 1000 * 60 * 20;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Set 'jwt.secret' (base64-encoded 256-bit key) in application.properties or environment variables to enable JWT generation.");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT secret loaded from configuration (base64). Using configured signing key.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode configured jwt.secret. Provide a valid base64-encoded 256-bit key.", e);
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        if (user.getRole() != null) {
            claims.put("role", user.getRole().name());
        }

        claims.put("tokenVersion", user.getTokenVersion());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public LoginResponse refreshToken(String refreshToken) {

        User user = userRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token expired. Please log in again.");
        }

        String newJwtToken = generateToken(user);

        return LoginResponse.builder()
                .token(newJwtToken)
                .refreshToken(refreshToken) // mantém o mesmo refresh token
                .tokenType("Bearer")
                .user(userMapper.toDTO(user))
                .build();
    }

    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

    // Note: token validation and parsing are handled by the OAuth2 Resource Server configuration (JwtDecoder).
    // Keep this service focused on token generation. If you need parsing utilities for tests or admin endpoints,
    // add them in a separate Test util or reintroduce minimal parsing helpers when necessary.
}
