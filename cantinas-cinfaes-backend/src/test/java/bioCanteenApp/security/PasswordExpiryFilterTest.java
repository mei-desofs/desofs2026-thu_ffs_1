package bioCanteenApp.security;

import bioCanteenApp.security.service.IPasswordService;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PasswordExpiryFilterTest {

    private IPasswordService passwordService;
    private UserRepo userRepository;
    private PasswordExpiryFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        passwordService = mock(IPasswordService.class);
        userRepository = mock(UserRepo.class);
        filter = new PasswordExpiryFilter(passwordService, userRepository);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_NoPrincipal_ShouldPassThrough() throws Exception {
        when(request.getUserPrincipal()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(passwordService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void doFilterInternal_UserNotFound_ShouldPassThrough() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("unknown@example.com");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(passwordService);
    }

    @Test
    void doFilterInternal_PasswordNotExpired_ShouldPassThrough() throws Exception {
        User user = new User("user@gmail.com", "Test User", "password");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@gmail.com");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordService.isPasswordExpired(user)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExpiredPasswordOnProtectedPath_ShouldReturn403() throws Exception {
        User user = new User("user@gmail.com", "Test User", "password");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@gmail.com");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.getRequestURI()).thenReturn("/api/canteens");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordService.isPasswordExpired(user)).thenReturn(true);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExpiredPasswordOnWhitelistedPath_ShouldPassThrough() throws Exception {
        User user = new User("user@gmail.com", "Test User", "password");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@gmail.com");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.getRequestURI()).thenReturn("/api/passwords/change");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordService.isPasswordExpired(user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_ExpiredPasswordOnLogoutPath_ShouldPassThrough() throws Exception {
        User user = new User("user@gmail.com", "Test User", "password");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@gmail.com");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.getRequestURI()).thenReturn("/api/auth/logout");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordService.isPasswordExpired(user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
