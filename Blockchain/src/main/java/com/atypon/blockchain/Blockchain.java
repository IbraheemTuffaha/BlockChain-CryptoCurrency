package com.atypon.blockchain;

import com.atypon.factory.BlockFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Vector;

/**
 * The implementation of the blockchain where it holds a vector
 * of {@link Block} holding {@link Blockable} data.
 * It's not thread safe.
 *
 * @param <T> The type of data used in the blockchain.
 *            could be anything bounded by {@link Blockable}.
 */
public final class Blockchain<T extends Blockable> implements Serializable, Cloneable {
    private Vector<Block<T>> blocks;

    /**
     * Constructor to initialize the vector 'blocks'.
     */
    public Blockchain() {
        blocks = new Vector<>();
    }

    /**
     * Verifies that the chain is legit by:
     * - Making sure no two blocks has the same data ID.
     * - The blocks are legit.
     * - The previous hash is equal to the previous block hash.
     *
     * @return true if the chain is legit, false otherwise.
     */
    public boolean verifyChain() {
        HashSet<String> foundIds = new HashSet<>();
        Block<T> prvBlock = null;
        for (Block<T> block : blocks) {
            // Check if the data ID is used more than once.
            String id = block.getDataId();
            if (foundIds.contains(id))
                return false;
            foundIds.add(id);

            // Check if the block is legit.
            if (!block.verifyBlock())
                return false;

            // Check if previous hash is equal to the previous block hash.
            if (prvBlock != null && !block.getPrvHash().equals(prvBlock.getHash()))
                return false;

            // Update previous block.
            prvBlock = block;

        }
        return true;
    }

    /**
     * Replaces the current blockchain with the given blockchain
     * if the new blockchain is longer and is verified to be legit.
     *
     * @param newBlockchain The new blockchain to replace.
     * @return true of the replacement was successful, false otherwise.
     */
    public boolean replaceChain(Blockchain<T> newBlockchain) {
        if (newBlockchain == null)
            return false;
        if (this == newBlockchain)
            return true;
        if (newBlockchain.length() <= this.length())
            return false;
        if (!newBlockchain.verifyChain())
            return false;
        this.blocks = new Vector<>(newBlockchain.blocks);
        return true;
    }

    /**
     * Add a block to the blockchain if legit.
     *
     * @param block The new block to add.
     * @return true of the block is added, false otherwise.
     */

    public boolean addBlock(Block<T> block) {
        if (blocks.isEmpty()) {
            if (!block.verifyFirstBlock())
                return false;
        } else {
            if (!block.verifyBlock())
                return false;
            if (!block.getPrvHash().equals(lastBlock().getHash()))
                return false;
        }

        blocks.add(block);

        // To ensure no previous block had same ID
        if (!verifyChain()) {
            removeLastBlock();
            return false;
        }

        return true;
    }

    /**
     * Overloaded version of addBlock that takes the data instead of a block.
     *
     * @param data The data in the new added block.
     * @return true if a block was added, false otherwise.
     */
    public boolean addBlock(T data) {
        if (blocks.isEmpty())
            return addBlock(BlockFactory.getFirstMinedInstance(data));
        return addBlock(BlockFactory.getMinedInstance(lastBlock(), data));
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the last block of the blockchain.
     *
     * @return The last block of the blockchain.
     * @throws IndexOutOfBoundsException if the blockchain is empty.
     */
    public Block<T> lastBlock() {
        return blocks.elementAt(blocks.size() - 1);
    }

    /**
     * Returns the first block of the blockchain.
     *
     * @return The first block of the blockchain.
     * @throws IndexOutOfBoundsException if the blockchain is empty.
     */
    public Block<T> firstBlock() {
        return blocks.elementAt(0);
    }

    /**
     * Get the length of the blockchain.
     *
     * @return The length of the blockchain.
     */
    public int length() {
        return blocks.size();
    }

    /**
     * Removes the last block of the blockchain.
     *
     * @throws IndexOutOfBoundsException if the blockchain is empty.
     */
    public void removeLastBlock() {
        blocks.removeElementAt(blocks.size() - 1);
    }

    public Vector<Block<T>> getBlocks() {
        return blocks;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public Blockchain<T> clone() {
        Blockchain<T> blockchain = new Blockchain<>();
        blockchain.blocks = new Vector<>(this.blocks);
        return blockchain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blockchain)) return false;
        Blockchain<?> blockchain = (Blockchain<?>) o;
        return Objects.equals(getBlocks(), blockchain.getBlocks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlocks());
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (Block<T> block : blocks)
            ret.append(block.toString()).append('\n');
        return "Blockchain{\n" +
                ret.toString() +
                '}';
    }


}
