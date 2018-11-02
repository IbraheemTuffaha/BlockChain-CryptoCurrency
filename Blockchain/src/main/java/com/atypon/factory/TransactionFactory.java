package com.atypon.factory;

import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A factory class for {@link Transaction} and {@link MinedTransaction}
 */
public final class TransactionFactory {

    /**
     * Creates a singed transaction with the given data.
     *
     * @param senderPublicKey   The sender public key.
     * @param receiverPublicKey The receiver public key.
     * @param amount            The amount of the transaction.
     * @param senderPrivateKey  The sender private key to sign the transaction.
     * @return A transaction with the given data.
     * @throws RuntimeException if the amount given was negative or the signature was invalid.
     */
    public static Transaction getInstance(PublicKey senderPublicKey, PublicKey receiverPublicKey,
                                          BigDecimal amount, PrivateKey senderPrivateKey) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("Transaction amount found negative!");
        Transaction transaction = new Transaction(KeyFactory.getUniqueId(), senderPublicKey, receiverPublicKey,
                amount, senderPrivateKey);
        if (!transaction.verifySignature())
            throw new RuntimeException("Transaction signature is invalid!");
        return transaction;
    }

    /**
     * Create a MinedTransaction given a Transaction and the extra info.
     *
     * @param transaction         The transaction info.
     * @param minerPublicKey      The miner public key.
     * @param miningFeePercentage The mining fee percentage.
     * @param miningReward        The mining reward.
     * @return A mined instance of the transaction
     */
    public static MinedTransaction getMinedInstance(Transaction transaction, PublicKey minerPublicKey,
                                                    BigDecimal miningFeePercentage, BigDecimal miningReward) {
        BigDecimal miningFee = transaction.getAmount().multiply(miningFeePercentage);
        return new MinedTransaction(transaction, minerPublicKey, miningFee, miningReward);
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private TransactionFactory() {
    }

}
