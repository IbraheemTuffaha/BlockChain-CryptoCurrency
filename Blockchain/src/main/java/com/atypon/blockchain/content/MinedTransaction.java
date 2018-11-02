package com.atypon.blockchain.content;

import com.atypon.utility.BitManipulation;
import com.atypon.utility.Hash;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.Objects;

/**
 * This class is an extension to {@link Transaction} class, where
 * it hold extra information about the miner and their reward.
 * Immutable thus threadsafe.
 */
public final class MinedTransaction extends Transaction {

    private final PublicKey minerPublicKey;
    // The miningFee is the deducted part of the transaction amount
    // given to the miner, miningReward is the reward given to the
    // miner in return for investing into mining.
    private final BigDecimal miningFee, miningReward;

    /**
     * Initialize the MinedTransaction given a transaction
     * and the extra information.
     *
     * @param transaction    The transaction to extend.
     * @param minerPublicKey The miner public key.
     * @param miningFee      The mining fee.
     * @param miningReward   The mining reward.
     */
    public MinedTransaction(Transaction transaction,
                            PublicKey minerPublicKey, BigDecimal miningFee, BigDecimal miningReward) {
        super(transaction.getId(), transaction.getSenderPublicKey(), transaction.getReceiverPublicKey(),
                transaction.getAmount(), transaction.getSignature());
        this.minerPublicKey = minerPublicKey;
        this.miningFee = miningFee;
        this.miningReward = miningReward;
    }

    /**
     * Overrides {@link Transaction#hash()} to include
     * the hash of the extra miner information.
     *
     * @return The hash of the data.
     */
    @Override
    public String hash() {
        return Hash.hash(super.hash(), super.getSignature(), getMinerPublicKeyString(),
                miningFee.toString(), miningReward.toString());
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public PublicKey getMinerPublicKey() {
        return minerPublicKey;
    }

    public BigDecimal getMiningFee() {
        return miningFee;
    }

    public BigDecimal getMiningReward() {
        return miningReward;
    }

    /**
     * Gets the actual value of the Key as a hexadecimal String.
     *
     * @return the miner public key.
     */
    public String getMinerPublicKeyString() {
        return BitManipulation.byteArrayToString(minerPublicKey.getEncoded());
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinedTransaction)) return false;
        if (!super.equals(o)) return false;
        MinedTransaction that = (MinedTransaction) o;
        return Objects.equals(getMinerPublicKey(), that.getMinerPublicKey()) &&
                Objects.equals(getMiningFee(), that.getMiningFee()) &&
                Objects.equals(getMiningReward(), that.getMiningReward());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMinerPublicKey(), getMiningFee(), getMiningReward());
    }

    @Override
    public String toString() {
        return super.toString() +
                "\n\tMinedTransaction{\n" +
                "\t\tminerPublicKey :" + getMinerPublicKeyString() + '\n' +
                "\t\tminingFee      :" + miningFee + '\n' +
                "\t\tminingReward   :" + miningReward + '\n' +
                "\t}";
    }


}
