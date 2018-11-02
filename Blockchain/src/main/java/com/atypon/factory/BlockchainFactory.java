package com.atypon.factory;

import com.atypon.blockchain.Block;
import com.atypon.blockchain.Blockable;
import com.atypon.blockchain.Blockchain;

/**
 * A factory class for {@link Blockchain}.
 */
public final class BlockchainFactory {

    /**
     * Creates an empty blockchain.
     * @param <T> The type of the blockchain, which is bounded to {@link Blockable}.
     * @return an empty blockchain.
     */
    public static <T extends Blockable> Blockchain<T> getInstance() {
        return new Blockchain<>();
    }

    /**
     * Creates a blockchain with on block containing the given data.
     * @param data The data of the first block, which is a {@link Blockable} object.
     * @param <T> The type of the blockchain, which is bounded to {@link Blockable}.
     * @return A blockchain with one block in it.
     */
    public static <T extends Blockable> Blockchain<T> getFirstInstance(T data) {
        Blockchain<T> blockchain = getInstance();
        Block<T> block = BlockFactory.getFirstMinedInstance(data);
        if (!blockchain.addBlock(block))
            throw new RuntimeException("Could not add first block!");
        return blockchain;
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private BlockchainFactory() {
    }
}
