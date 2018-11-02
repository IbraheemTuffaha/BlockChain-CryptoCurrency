package com.atypon.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.atypon.utility.BitManipulation.*;

/**
 * A utility class to help deal with hashing a string or multiple
 * strings with the 'SHA-256' hashing algorithm.
 */
public final class Hash {
    // Built-in class that hashes a byte array.
    private final static String ALGORITHM = "SHA-256";

    /**
     * Hash the given string and convert the result to a string.
     *
     * @param data The string to be hashed.
     * @return The hash in a hexadecimal string form.
     */
    public static String hash(String data) {
        try {
            return byteArrayToString(MessageDigest.getInstance(ALGORITHM).digest(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Takes 2 strings, hashes them and then re-hashes the
     * concatenation of the hashes to insure hash security.
     *
     * @param dataA First string to be hashed.
     * @param dataB Second string to be hashed.
     * @return The hash of the 2 strings combined.
     */
    private static String hashAndCombine(String dataA, String dataB) {
        String hashA = hash(dataA);
        String hashB = hash(dataB);
        return hash(hashA + hashB);
    }

    /**
     * Hashes and combines an array of strings
     * using the hashAndCombine method.
     *
     * @param data The array of strings to be hashed.
     * @return The hash of the strings combined.
     */
    public static String hash(String... data) {
        if (data == null || data.length == 0)
            return hash("");
        String result = hash(data[0]);
        for (int i = 1; i < data.length; ++i)
            result = hashAndCombine(result, data[i]);
        return result;
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private Hash() {
    }
}
