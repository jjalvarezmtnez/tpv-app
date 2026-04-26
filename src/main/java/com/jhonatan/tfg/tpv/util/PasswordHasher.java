package com.jhonatan.tfg.tpv.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase utilitaria encargada de generar y verificar hashes de contraseñas.
 *
 * En este proyecto se utiliza SHA-256 para evitar almacenar contraseñas
 * en texto plano dentro de la base de datos.
 *
 * IMPORTANTE:
 * Este enfoque es válido para proyectos académicos, pero en entornos reales
 * se recomienda utilizar algoritmos adaptativos como BCrypt o Argon2.
 */
public final class PasswordHasher {

    /**
     * Constructor privado para evitar instanciación.
     */
    private PasswordHasher() {
    }

    /**
     * Genera el hash SHA-256 de una contraseña en texto plano.
     *
     * Proceso:
     * 1. Convierte la contraseña a bytes (UTF-8)
     * 2. Aplica el algoritmo SHA-256
     * 3. Convierte el resultado a hexadecimal legible
     *
     * @param password contraseña en texto plano
     * @return hash de la contraseña en formato hexadecimal
     * @throws IllegalStateException si SHA-256 no está disponible
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = messageDigest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "El algoritmo SHA-256 no está disponible.",
                    e
            );
        }
    }

    /**
     * Verifica si una contraseña coincide con un hash almacenado.
     *
     * @param plainPassword contraseña introducida por el usuario
     * @param storedHash hash almacenado en la base de datos
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHash);
    }

    /**
     * Convierte un array de bytes en una cadena hexadecimal.
     *
     * @param bytes array de bytes generado por el algoritmo hash
     * @return representación hexadecimal del hash
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}