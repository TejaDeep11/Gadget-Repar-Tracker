package com.pes.gadgetrepair.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 Utility Class

 Purpose:
 Provides password hashing and verification.

 Security Note:
 In real production systems use:
 - BCrypt
 - Argon2
 - Spring Security

 This simplified implementation is suitable
 for academic demonstration purposes.
*/

public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hashPassword(String password) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash =
                    digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {

        String hashedInput = hashPassword(rawPassword);

        return hashedInput.equals(hashedPassword);
    }
}