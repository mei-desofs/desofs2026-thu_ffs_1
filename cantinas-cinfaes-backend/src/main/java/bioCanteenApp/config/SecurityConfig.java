package bioCanteenApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/passwords/recover-password").permitAll()
                        .requestMatchers("/api/passwords/reset-password").permitAll()
                        .requestMatchers("/api/passwords/change").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/approval").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/reject").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/edit").hasRole("ADMIN") // no code
                        .requestMatchers("/api/suppliers/deactivate").hasRole("ADMIN") //no code
                        .requestMatchers("/api/menus/**").hasRole("DIETITIAN") // no code para o edit menu
                        .requestMatchers("/api/provisioning/**").hasRole("CANTEEN_MANAGER")
                        .anyRequest().authenticated()
                )
                .build();
    }
}
