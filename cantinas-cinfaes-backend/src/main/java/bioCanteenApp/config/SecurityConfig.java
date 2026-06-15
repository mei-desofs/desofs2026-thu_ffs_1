package bioCanteenApp.config;

import bioCanteenApp.users.repository.IUserRepo;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtDecoder tokenVersionValidatingDecoder(IUserRepo userRepo) {
        NimbusJwtDecoder delegate = NimbusJwtDecoder
                .withSecretKey(new javax.crypto.spec.SecretKeySpec(
                        Decoders.BASE64.decode(jwtSecret), "HmacSHA256"))
                .build();

        return token -> {
            var jwt = delegate.decode(token);

            String email = jwt.getSubject();
            long tokenVersion = ((Number) jwt.getClaim("tokenVersion")).longValue();

            userRepo.findByEmail(email).ifPresent(user -> {
                if (tokenVersion != user.getTokenVersion()) {
                    throw new org.springframework.security.oauth2.jwt.BadJwtException(
                            "Session invalidated. Please log in again.");
                }
            });

            return jwt;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, IUserRepo userRepo) throws Exception {

        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/**")))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Endpoints Públicos ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/passwords/recover-password").permitAll()
                        .requestMatchers("/api/passwords/reset-password").permitAll()
                        .requestMatchers("/api/suppliers/apply").permitAll()

                        // --- Endpoints Autenticados (Qualquer User Logado) ---
                        .requestMatchers("/api/passwords/change").authenticated()

                        // --- Endpoints Restritos por Role ---
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Foram adicionados os ** e os prefixos corretos aqui:
                        .requestMatchers("/api/suppliers/approve/**").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/reject/**").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/application/*/certificate").hasRole("ADMIN")

                        .requestMatchers("/api/suppliers/edit").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/deactivate").hasRole("ADMIN")

                        .requestMatchers("/api/menus/**").hasRole("DIETITIAN")
                        .requestMatchers("/api/provisioning/**").hasRole("CANTEEN_MANAGER")

                        // --- O resto obriga sempre a estar logado ---
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(authenticationConverter)
                                .decoder(tokenVersionValidatingDecoder(userRepo)))
                )
                .build();
    }
}