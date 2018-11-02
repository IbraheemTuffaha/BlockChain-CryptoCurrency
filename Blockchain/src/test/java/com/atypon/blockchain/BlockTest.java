package com.atypon.blockchain;

import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;
import com.atypon.factory.BlockFactory;
import com.atypon.factory.KeyFactory;
import com.atypon.factory.TransactionFactory;
import com.atypon.utility.Randomize;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.KeyPair;

import static org.junit.Assert.*;

public class BlockTest {
    private MinedTransaction tranWithMiner;
    private Block<MinedTransaction> firstBlock, secondBlock;

    @Before
    public void setUp() {
        KeyPair sender = KeyFactory.getKeyInstance();
        KeyPair receiver = KeyFactory.getKeyInstance();
        KeyPair miner = KeyFactory.getKeyInstance();
        BigDecimal amount = BigDecimal.valueOf(Randomize.randDouble());

        Transaction tran = TransactionFactory.getInstance(sender.getPublic(), receiver.getPublic(),
                amount, sender.getPrivate());
        tranWithMiner = TransactionFactory.getMinedInstance(tran, miner.getPublic(),
                BigDecimal.valueOf(Randomize.randRatio()), BigDecimal.valueOf(Randomize.randDouble()));

    }

    @Test
    public void blockTest() {

        firstBlock = BlockFactory.getFirstMinedInstance(tranWithMiner);
        secondBlock = BlockFactory.getMinedInstance(firstBlock, tranWithMiner);

        blockCreationTest();
        blockDataSetTest();
        previousHashTest();

    }

    private void blockCreationTest() {
        assertTrue(firstBlock.verifyBlock());
        assertTrue(secondBlock.verifyBlock());
        long tmp = secondBlock.getNonce();
        secondBlock.setNonce(342L);
        assertFalse(secondBlock.verifyBlock());
        secondBlock.setNonce(tmp);
    }

    private void blockDataSetTest() {
        assertEquals(tranWithMiner, firstBlock.getData());
        assertEquals(tranWithMiner, secondBlock.getData());
    }

    private void previousHashTest() {
        assertEquals(firstBlock.getHash(), secondBlock.getPrvHash());
    }

}