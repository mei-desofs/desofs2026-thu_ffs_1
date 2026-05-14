package bioCanteenApp.security;

import bioCanteenApp.users.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Generate a secure key for HS256
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Default expiration is 24 hours
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        if (user.getRole() != null) {
            claims.put("role", user.getRole().name());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
}
