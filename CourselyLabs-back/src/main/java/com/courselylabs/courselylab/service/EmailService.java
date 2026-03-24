package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.entity.VerificationTokenEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.UserRepository;
import com.courselylabs.courselylab.repository.VerificationTokenRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Transactional
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final String fromEmail;
    private final String frontendUrl;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    public EmailService(JavaMailSender mailSender,
                        VerificationTokenRepository tokenRepository,
                        UserRepository userRepository,
                        @Value("${spring.mail.from:noreply@courselylabs.com}") String fromEmail,
                        @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.fromEmail = fromEmail;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(UserEntity user) {
        String token = UUID.randomUUID().toString();

        VerificationTokenEntity verificationToken = new VerificationTokenEntity();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        verificationToken.setUsed(false);
        tokenRepository.save(verificationToken);

        String verifyUrl = frontendUrl + "/verificar-email?token=" + token;
        String subject = "Verifica tu cuenta en CourselyLabs";
        String body = buildVerificationHtml(user.getFirstName(), verifyUrl);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    public void verifyEmail(String token) {
        VerificationTokenEntity verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token de verificacion invalido"));

        if (Boolean.TRUE.equals(verificationToken.getUsed())) {
            throw new BadRequestException("Este enlace ya ha sido utilizado");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El enlace de verificacion ha expirado. Solicita uno nuevo.");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        UserEntity user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);
    }

    public void resendVerification(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException("Tu cuenta ya esta verificada");
        }

        sendVerificationEmail(user);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email de verificacion", e);
        }
    }

    private String buildVerificationHtml(String name, String url) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #0f766e, #0d9488); padding: 30px; border-radius: 8px 8px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">CourselyLabs</h1>
                </div>
                <div style="background: #ffffff; padding: 30px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;">
                    <h2 style="color: #1f2937; margin-top: 0;">Hola %s,</h2>
                    <p style="color: #4b5563; line-height: 1.6;">
                        Gracias por registrarte en CourselyLabs. Para activar tu cuenta, haz clic en el siguiente boton:
                    </p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s"
                           style="background-color: #0f766e; color: white; padding: 12px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">
                            Verificar mi cuenta
                        </a>
                    </div>
                    <p style="color: #6b7280; font-size: 14px;">
                        Este enlace expira en 24 horas. Si no has creado una cuenta, puedes ignorar este email.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                        CourselyLabs — Plataforma de cursos online
                    </p>
                </div>
            </div>
            """.formatted(name, url);
    }
}
