package com.courselylabs.courselylab.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Cifrado at-rest de secretos pequeños almacenados en BD (token de echo, etc).
 *
 * Usa AES-256 con BouncyCastle vía Spring Security {@link Encryptors#delux(CharSequence, CharSequence)}:
 * - 256-bit cipher key derivada por PBKDF2 a partir de {@code app.encryption.secret} + salt.
 * - IV aleatorio en cada operación.
 *
 * La salt es hexadecimal (mismo formato que {@code KeyGenerators.string()}).
 *
 * En producción la clave debe configurarse vía variables de entorno
 * (APP_ENCRYPTION_SECRET, APP_ENCRYPTION_SALT) y nunca commitearse.
 */
@Component
public class SecretEncryptor {

    private final TextEncryptor encryptor;

    public SecretEncryptor(
            @Value("${app.encryption.secret}") String secret,
            @Value("${app.encryption.salt}") String saltHex) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.encryption.secret no configurado");
        }
        if (saltHex == null || saltHex.isBlank()) {
            throw new IllegalStateException("app.encryption.salt no configurado");
        }
        this.encryptor = Encryptors.delux(secret, saltHex);
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        return encryptor.encrypt(plain);
    }

    public String decrypt(String cipher) {
        if (cipher == null) return null;
        return encryptor.decrypt(cipher);
    }
}
