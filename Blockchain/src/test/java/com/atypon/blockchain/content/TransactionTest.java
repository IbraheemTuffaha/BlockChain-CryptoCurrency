package com.atypon.blockchain.content;

import com.atypon.factory.KeyFactory;
import com.atypon.factory.TransactionFactory;
import com.atypon.utility.Randomize;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.KeyPair;

import static org.junit.Assert.*;

public class TransactionTest {
    private String id;
    private KeyPair senderKeyPair;
    private KeyPair receiverKeyPair;
    private KeyPair minerKeyPair;
    private BigDecimal amount;
    private Transaction transaction;
    private MinedTransaction minedTransaction;
    private BigDecimal feesPercentage;
    private BigDecimal miningReward;

    @Before
    public void setUp() {
        id = KeyFactory.getUniqueId();
        senderKeyPair = KeyFactory.getKeyInstance();
        receiverKeyPair = KeyFactory.getKeyInstance();
        minerKeyPair = KeyFactory.getKeyInstance();
        amount = BigDecimal.valueOf(Randomize.randDouble());
        feesPercentage = BigDecimal.valueOf(Randomize.randRatio());
        miningReward = BigDecimal.valueOf(Randomize.randDouble());

        transaction = TransactionFactory.getInstance(senderKeyPair.getPublic(), receiverKeyPair.getPublic()
                , amount, senderKeyPair.getPrivate());
        minedTransaction = TransactionFactory.getMinedInstance(transaction,
                minerKeyPair.getPublic(), feesPercentage, miningReward);
    }

    // Transaction Class + Factory
    @Test
    public void createSignedTransactionTest() {
        // Test Transaction constructor
        Transaction transaction = new Transaction(id, senderKeyPair.getPublic(), receiverKeyPair.getPublic(),
                amount, senderKeyPair.getPrivate());
        assertTrue(transaction.verifySignature());
        assertEquals(transaction.getAmount(), amount);

        // Test TransactionFactory getInstance
        assertTrue(this.transaction.verifySignature());
        assertEquals(this.transaction.getAmount(), amount);
    }

    @Test
    public void hashConsistencyTest() {
        assertEquals(transaction.hash(), transaction.hash());
    }

    @Test
    public void equalsTransactionTest() {
        assertEquals(transaction, new Transaction(transaction.getId(), senderKeyPair.getPublic(),
                receiverKeyPair.getPublic(), amount, senderKeyPair.getPrivate()));
        assertEquals(new Transaction(transaction.getId(), senderKeyPair.getPublic(), receiverKeyPair.getPublic(),
                amount, senderKeyPair.getPrivate()), transaction);
    }

    // MinedTransaction Class + Factory

    @Test
    public void createMinedTransactionTest() {

        assertTrue(minedTransaction.verifySignature());
        assertEquals(minedTransaction.getMiningFee(), minedTransaction.getAmount().multiply(feesPercentage));
        assertEquals(minedTransaction.getMiningReward(), miningReward);
        assertEquals(minedTransaction.getAmount(), amount);

    }

    @Test
    public void equalsMinedTransactionTest() {
        assertEquals(minedTransaction, TransactionFactory.getMinedInstance(transaction,
                minerKeyPair.getPublic(), feesPercentage, miningReward));
        assertEquals(TransactionFactory.getMinedInstance(transaction,
                minerKeyPair.getPublic(), feesPercentage, miningReward), minedTransaction);
    }

    @Test
    public void minedHashConsistencyTest() {
        assertEquals(minedTransaction.hash(), minedTransaction.hash());
        // Hash of the parts from parent is equal to the parent hash
        assertEquals(minedTransaction.getHash(), transaction.hash());
        // Since hash() is overridden in MinedTransaction, it should give different output.
        assertNotEquals(minedTransaction.hash(), transaction.hash());
    }


}