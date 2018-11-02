package com.atypon.blockchain;

/**
 * An interface to mark the data that can be used as a part of the blockchain block.
 * Guarantees that the existence of the functions:
 * - {@link Blockable#hash()}.
 * - {@link Blockable#verifySignature()}.
 * - {@link Blockable#getId()}.
 */
public interface Blockable {
    /**
     * Hash the data.
     *
     * @return the hash of the data.
     */
    String hash();

    /**
     * Verify the signature.
     *
     * @return true if the signature is correct.
     */
    boolean verifySignature();

    /**
     * Get the ID of the data.
     *
     * @return The ID of the data, which should be unique.
     */
    String getId();
}
