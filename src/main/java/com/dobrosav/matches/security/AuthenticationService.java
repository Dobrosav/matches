package com.dobrosav.matches.security;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.db.entities.RefreshToken;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.repos.RefreshTokenRepository;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.exception.ErrorType;
import com.dobrosav.matches.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticationService(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthenticationResponse register(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ServiceException(ErrorType.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        var user = User.createDefaultUser(
                request.getName(),
                request.getSurname(),
                request.getEmail(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getSex(),
                request.getDateOfBirth(),
                request.getDisabilities()
        );
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(savedUser.getEmail());
        var refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());
        saveUserRefreshToken(savedUser, refreshToken);
        return new AuthenticationResponse(jwtToken, refreshToken);
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        try {
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
            log.info("User found: {}", user.getEmail());
            log.info("Encoded password from DB: {}", user.getPassword());
            log.info("Raw password from request: {}", request.getPassword());
            log.info("Encoded password from request: {}", passwordEncoder.encode(request.getPassword()));
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new ServiceException(ErrorType.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var jwtToken = jwtService.generateToken(user.getEmail());
        var refreshToken = jwtService.generateRefreshToken(user.getEmail());
        saveUserRefreshToken(user, refreshToken);
        return new AuthenticationResponse(jwtToken, refreshToken);
    }

    private void saveUserRefreshToken(User user, String refreshToken) {
        var refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiryDate(Instant.now().plusMillis(jwtService.refreshTokenExpiration));
        refreshTokenRepository.save(refreshTokenEntity);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServiceException(ErrorType.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
            if (jwtService.isTokenValid(refreshToken, user.getEmail())) {
                var accessToken = jwtService.generateToken(user.getEmail());
                var newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
                saveUserRefreshToken(user, newRefreshToken);
                return new AuthenticationResponse(accessToken, newRefreshToken);
            }
        }
        throw new ServiceException(ErrorType.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
