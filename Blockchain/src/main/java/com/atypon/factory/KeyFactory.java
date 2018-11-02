package com.atypon.factory;

import java.security.*;
import java.util.UUID;

/**
 * A factory class that generates {@link KeyPair} and unique IDs.
 */
public final class KeyFactory {
    private final static int KEY_LENGTH = 1024;
    private final static String ALGORITHM = "RSA";
    private final static KeyPairGenerator KEY_PAIR_GENERATOR = createKeyPairGenerator();

    private static KeyPairGenerator createKeyPairGenerator() {
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance(ALGORITHM);
            instance.initialize(KEY_LENGTH);
            return instance;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a Public/Private key pair.
     *
     * @return The generated KeyPair.
     */
    public static KeyPair getKeyInstance() {
        KeyPair keyPair = null;
        if (KEY_PAIR_GENERATOR != null)
            keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        return keyPair;
    }

    /**
     * Generate a unique 256-bit ID.
     *
     * @return A unique ID.
     */
    public static String getUniqueId() {
        return UUID.randomUUID().toString() + '-' + UUID.randomUUID().toString();
    }

    /**
     * A private constructor to enforce non-instantiability.
     */

    private KeyFactory() {
    }
}
