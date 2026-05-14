package bioCanteenApp.security;

import bioCanteenApp.security.service.IPasswordService;
import bioCanteenApp.users.repository.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * REQ 2.5
 * The system obliges the user to after their password every 6 months.
 */
@Component
public class PasswordExpiryFilter extends OncePerRequestFilter {

    // If the password is expired, the user can only access the following endpoints
    private static final String[] WHITELISTED_PATHS = {
            "/api/passwords/change",
            "/api/passwords/reset-password",
            "/api/passwords/recover-password",
            "/api/auth/logout"
    };

    private final IPasswordService passwordService;
    private final UserRepo userRepository;

    public PasswordExpiryFilter(IPasswordService passwordService, UserRepo userRepository) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

        if (username != null) {
            var user = userRepository.findByEmail(username).orElse(null);
            if (user != null && passwordService.isPasswordExpired(user)) {
                String requestPath = request.getRequestURI();
                boolean isWhitelisted = false;
                for (String path : WHITELISTED_PATHS) {
                    if (requestPath.startsWith(path)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                if (!isWhitelisted) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\": \"Password expired. "
                            + "You must change your password every 6 months. "
                            + "Please use POST /api/passwords/change.\"}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
