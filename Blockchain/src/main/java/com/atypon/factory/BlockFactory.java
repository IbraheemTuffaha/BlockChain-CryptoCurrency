package com.atypon.factory;

import com.atypon.blockchain.Block;
import com.atypon.blockchain.Blockable;

import java.util.Random;

/**
 * A factory class for {@link Block}.
 */
public final class BlockFactory {
    /**
     * Creates a mined instance of the given data.
     *
     * @param prvBlock      The previous block in the chain.
     * @param data          The data of the current block.
     * @param numberOfZeros The number of zeros needed to proof work.
     * @param <T>           The type of the blockchain, which is bounded to {@link Blockable}.
     * @return A mined instance of the given data.
     * @throws RuntimeException if the proof of work is invalid or the signature is invalid.
     */
    public static <T extends Blockable> Block<T> getMinedInstance(Block<T> prvBlock, T data,
                                                                  int numberOfZeros) {
        if (!data.verifySignature())
            throw new RuntimeException("Data signature is invalid!");
        Block<T> block = getInstance(prvBlock, data);
        block = BlockFactory.mine(block, numberOfZeros);
        if (block != null && !block.verifyProofOfWork())
            throw new RuntimeException("Block is not proved to have been worked on!");
        return block;
    }

    /**
     * An overloaded version that skips the number of zeros
     * and sends the default number of zeroes.
     *
     * @param prvBlock The previous block in the chain.
     * @param data     The data of the current block.
     * @param <T>      The type of the blockchain, which is bounded to {@link Blockable}.
     * @return A mined instance of the given data.
     */
    public static <T extends Blockable> Block<T> getMinedInstance(Block<T> prvBlock, T data) {
        return getMinedInstance(prvBlock, data, Block.NUMBER_OF_LEADING_ZEROS);
    }

    /**
     * An overloaded version that skips the number of zeros
     * and the previous hash and sends the default number of
     * zeroes since it is created for the first block in the blockchain
     *
     * @param data The data of the current block.
     * @param <T>  The type of the blockchain, which is bounded to {@link Blockable}.
     * @return A mined instance of the given data.
     */
    public static <T extends Blockable> Block<T> getFirstMinedInstance(T data) {
        return getMinedInstance(null, data, Block.FIRST_BLOCK_NUMBER_OF_LEADING_ZEROS);
    }

    /**
     * This is the version that actually creates the block.
     *
     * @param prvHash The hash of the previous block in the chain.
     * @param data    The data of the current block.
     * @param <T>     The type of the blockchain, which is bounded to {@link Blockable}.
     * @return An instance of a block with the given data.
     */
    public static <T extends Blockable> Block<T> getInstance(String prvHash, T data) {
        return new Block<>(prvHash, data, 0L);
    }

    /**
     * An overloaded version that checks whether there is a previous block or not.
     *
     * @param prvBlock The previous block in the chain.
     * @param data     The data of the current block.
     * @param <T>      The type of the blockchain, which is bounded to {@link Blockable}.
     * @return An instance of a block with the given data.
     */
    public static <T extends Blockable> Block<T> getInstance(Block<T> prvBlock, T data) {
        return getInstance(prvBlock == null ? "" : prvBlock.getHash(), data);
    }

    /**
     * A function that mines a given block to proof the work for a specific number of zeros.
     *
     * @param block         The block to mine.
     * @param numberOfZeros The number of zeros needed to proof work.
     * @param <T>           The type of the blockchain, which is bounded to {@link Blockable}.
     * @return The block after being mined.
     */
    private static <T extends Blockable> Block<T> mine(Block<T> block, int numberOfZeros) {
        Block<T> newBlock = BlockFactory.getInstance(block.getPrvHash(), block.getData());
        Long nonce = new Random().nextLong();
        synchronized (BlockFactory.class) {
            run();
            do {
                if (isStop())
                    return null;
                ++nonce;
            } while (!newBlock.verifyProofOfWork(nonce, numberOfZeros));
        }
        newBlock.setNonce(nonce);
        return newBlock;
    }

    // A boolean indicating the running of a mining process.
    private static boolean isRunning = true;

    /**
     * A static function to check if a block is being mined.
     *
     * @return true if a block is being mined, false otherwise.
     */
    private static boolean isStop() {
        return !isRunning;
    }

    /**
     * Stops the mining of block if any.
     */
    public static void stop() {
        isRunning = false;
    }

    /**
     * Allows the mining of a block if any.
     */
    private static void run() {
        isRunning = true;
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private BlockFactory() {
    }
}
