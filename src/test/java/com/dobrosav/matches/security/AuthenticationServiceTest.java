package com.dobrosav.matches.security;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.repos.RefreshTokenRepository;
import com.dobrosav.matches.db.repos.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void whenRegister_thenReturnsAuthenticationResponse() {
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User savedUser = new User();
        savedUser.setEmail("test@example.com");

        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("test-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("test-refresh-token");

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void whenAuthenticate_thenReturnsAuthenticationResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepo.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("test-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("test-refresh-token");


        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response.getAccessToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void whenRefreshToken_thenReturnsNewTokens() {
        final String refreshToken = "test-refresh-token";
        final String userEmail = "test@example.com";
        User user = new User();
        user.setEmail(userEmail);

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(userEmail);
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");

        AuthenticationResponse response = authenticationService.refreshToken(httpServletRequest);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
}
