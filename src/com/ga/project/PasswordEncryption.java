package com.ga.project;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class PasswordEncryption {

    public static String hashPassword(String password) {
        try {
            // Get an instance of the MessageDigest with SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Hash the password bytes
            byte[] encoded_hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder(2 * encoded_hash.length);
            for (byte b : encoded_hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Handle the case where the algorithm is not available
            e.printStackTrace();
            return null; // Or throw a custom exception
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHashedPassword) {
        String hashedRawPassword = hashPassword(rawPassword);
        return hashedRawPassword != null && hashedRawPassword.equals(storedHashedPassword);
    }

    public static void main(String[] args) {
        String userPassword = "mySecretPassword123";
        String hashedPassword = hashPassword(userPassword);
        System.out.println("Hashed password: " + hashedPassword);

        // Simulate verification
        boolean isValid = verifyPassword("mySecretPassword123", hashedPassword);
        System.out.println("Password verification successful: " + isValid);

        boolean isInvalid = verifyPassword("wrongPassword", hashedPassword);
        System.out.println("Password verification with wrong password: " + isInvalid);
    }
}