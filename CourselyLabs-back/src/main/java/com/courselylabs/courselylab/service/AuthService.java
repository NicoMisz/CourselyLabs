package com.courselylabs.courselylab.service;

import com.courselylabs.courselylab.dto.ChangePasswordDTO;
import com.courselylabs.courselylab.dto.auth.AuthResponseDTO;
import com.courselylabs.courselylab.dto.auth.LoginRequestDTO;
import com.courselylabs.courselylab.dto.auth.RegisterRequestDTO;
import com.courselylabs.courselylab.entity.PasswordResetTokenEntity;
import com.courselylabs.courselylab.entity.RefreshTokenEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.UnauthorizedException;
import com.courselylabs.courselylab.mapper.UserMapper;
import com.courselylabs.courselylab.repository.PasswordResetTokenRepository;
import com.courselylabs.courselylab.repository.RefreshTokenRepository;
import com.courselylabs.courselylab.repository.UserRepository;
import com.courselylabs.courselylab.security.JwtService;
import com.courselylabs.courselylab.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final long refreshExpiration;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       EmailService emailService,
                       @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.refreshExpiration = refreshExpiration;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return buildAuthResponse(userDetails);
    }

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Ya existe un usuario con el email '" + dto.getEmail() + "'");
        }

        UserEntity user = new UserEntity();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setBio(dto.getBio());
        user.setRole("user");
        user.setIsVerified(false);
        user.setIsActive(true);

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user);
        } catch (Exception e) {
            // No bloquear el registro si falla el envío de email
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        return buildAuthResponse(userDetails);
    }

    public AuthResponseDTO refresh(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        if (refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(refreshToken.getUser());
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .expiresIn(refreshExpiration / 1000)
                .user(userMapper.toDTO(refreshToken.getUser()))
                .build();
    }

    public void changePassword(String email, ChangePasswordDTO dto) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    public void requestPasswordReset(String email) {
        // No revelamos si el email existe o no por seguridad: respondemos OK siempre.
        userRepository.findByEmail(email).ifPresent(user -> {
            try {
                emailService.sendPasswordResetEmail(user);
            } catch (Exception e) {
                // No bloquear si falla el envío de email.
            }
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token de restablecimiento inválido"));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new BadRequestException("Este enlace ya ha sido utilizado");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El enlace ha expirado. Solicita uno nuevo.");
        }

        UserEntity user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidar todas las sesiones existentes del usuario por seguridad.
        refreshTokenRepository.deleteByUser(user);
    }

    private AuthResponseDTO buildAuthResponse(UserDetailsImpl userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setUser(userDetails.getUser());
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(refreshExpiration / 1000)
                .user(userMapper.toDTO(userDetails.getUser()))
                .build();
    }
}
