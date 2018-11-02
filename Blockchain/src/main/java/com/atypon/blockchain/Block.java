package com.atypon.blockchain;

import com.atypon.utility.BitManipulation;
import com.atypon.utility.Hash;

import java.io.Serializable;
import java.util.Objects;

/**
 * The instance of a block in the blockchain that holds a generic
 * {@link Blockable} data as well as the hash of the previous block
 * in the chain and the 'nonce' which is added to balance the hash
 * and prove the work on this block.
 * Threadsafe one mined since only the `nonce` can be changed,
 * but is only changed while mining.
 *
 * @param <T> The type of data used in the block.
 *            could be anything bounded by {@link Blockable}.
 */
public final class Block<T extends Blockable> implements Serializable {
    // The number of leading zeros for the first block in the chain,
    // this would make it harder to make a new blockchain to replace
    // the already existing one since the first block need more zeros.
    public final static int FIRST_BLOCK_NUMBER_OF_LEADING_ZEROS = 12;
    // The number of leading zeros for the other blocks.
    public final static int NUMBER_OF_LEADING_ZEROS = 6;
    // The hash of the previous block.
    private final String prvHash;
    // The blockable data.
    private final T data;
    // The nonce used to balance the hash.
    private Long nonce;

    /**
     * Initialize a block.
     *
     * @param prvHash The hash of the previous block.
     * @param data    The blockable data.
     * @param nonce   The nonce of the block.
     */
    public Block(String prvHash, T data, Long nonce) {
        this.prvHash = prvHash;
        this.data = data;
        this.nonce = nonce;
    }

    /**
     * Verifies that the block's hash contains 'numberOfLeadingZeros'
     * leading zeros given the nonce.
     *
     * @param nonce                The nonce to test.
     * @param numberOfLeadingZeros The number of leading zeros.
     * @return true if the hash contains the needed leading zeros, false otherwise.
     */
    public boolean verifyProofOfWork(Long nonce, int numberOfLeadingZeros) {
        return BitManipulation.getFirstBits(numberOfLeadingZeros,
                getHash(nonce)).equals(BitManipulation.getLeadingZeros(numberOfLeadingZeros));
    }

    private boolean verifyProofOfWork(int numberOfLeadingZeros) {
        return verifyProofOfWork(nonce, numberOfLeadingZeros);
    }

    public boolean verifyProofOfWork() {
        return verifyProofOfWork(NUMBER_OF_LEADING_ZEROS);
    }

    /**
     * Verifies the block as it is the first block in the chain
     * by checking the data signature and the proof of work.
     *
     * @return true if the block is valid to be the first block, false otherwise.
     */
    public boolean verifyFirstBlock() {
        return data.verifySignature() && verifyProofOfWork(FIRST_BLOCK_NUMBER_OF_LEADING_ZEROS);
    }

    /**
     * Verifies the block by checking the data signature and the proof of work.
     *
     * @return true if the block is valid to be in the chain, false otherwise.
     */
    public boolean verifyBlock() {
        return data.verifySignature() && verifyProofOfWork();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public String getPrvHash() {
        return prvHash;
    }

    public T getData() {
        return data;
    }

    public Long getNonce() {
        return nonce;
    }

    private String getHash(Long nonce) {
        return Hash.hash(prvHash, nonce.toString(), data.hash());
    }

    public String getHash() {
        return getHash(this.nonce);
    }

    public String getDataId() {
        return data.getId();
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block<?> block = (Block<?>) o;
        return Objects.equals(getPrvHash(), block.getPrvHash()) &&
                Objects.equals(getData(), block.getData()) &&
                Objects.equals(getNonce(), block.getNonce());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrvHash(), getData(), getNonce());
    }

    @Override
    public String toString() {
        return "Block{\n" +
                "\tprvHash :" + prvHash + '\n' +
                "\tdata    :" + data + '\n' +
                "\tnonce   :" + nonce + '\n' +
                "\thash    :" + getHash() + '\n' +
                '}';
    }
}
