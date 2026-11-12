package com.acme.payroll.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Salted SHA-256 password hashing.
 * TODO: move to PBKDF2 when we drop the old user import tool.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private PasswordUtil() {
    }

    public static String newSalt() {
        byte[] b = new byte[8];
        RANDOM.nextBytes(b);
        return toHex(b);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + ":" + password).getBytes("UTF-8"));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(hash(password, salt).getBytes(), expectedHash.getBytes());
    }

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            out[i * 2] = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0f];
        }
        return new String(out);
    }
}
