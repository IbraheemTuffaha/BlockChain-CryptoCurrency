package com.atypon.blockchain;

import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;
import com.atypon.factory.BlockFactory;
import com.atypon.factory.BlockchainFactory;
import com.atypon.factory.KeyFactory;
import com.atypon.factory.TransactionFactory;
import com.atypon.utility.Randomize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.KeyPair;

import static org.junit.Assert.*;

public class BlockchainTest {
    private Blockchain<MinedTransaction> blockchain;
    private MinedTransaction tranWithMiner1, tranWithMiner2;

    @Before
    public void setUp() {
        KeyPair sender = KeyFactory.getKeyInstance();
        KeyPair receiver = KeyFactory.getKeyInstance();
        KeyPair miner = KeyFactory.getKeyInstance();
        BigDecimal amount = BigDecimal.valueOf(Randomize.randDouble());

        Transaction tran = TransactionFactory.getInstance(sender.getPublic(), receiver.getPublic(),
                amount, sender.getPrivate());
        MinedTransaction tranWithMiner0 = TransactionFactory.getMinedInstance(tran, miner.getPublic(),
                BigDecimal.valueOf(Randomize.randRatio()), BigDecimal.valueOf(Randomize.randDouble()));

        tran = TransactionFactory.getInstance(sender.getPublic(), receiver.getPublic(),
                amount, sender.getPrivate());
        tranWithMiner1 = TransactionFactory.getMinedInstance(tran, miner.getPublic(),
                BigDecimal.valueOf(Randomize.randRatio()), BigDecimal.valueOf(Randomize.randDouble()));
        tran = TransactionFactory.getInstance(sender.getPublic(), receiver.getPublic(),
                amount, sender.getPrivate());
        tranWithMiner2 = TransactionFactory.getMinedInstance(tran, miner.getPublic(),
                BigDecimal.valueOf(Randomize.randRatio()), BigDecimal.valueOf(Randomize.randDouble()));


        blockchain = BlockchainFactory.getFirstInstance(tranWithMiner0);
    }

    @Test
    public void chainTest() {
        firstBlockTest();
        verifyChainTest();
        cloneTest();
        addBlock();
        replaceChainTest();
    }

    private void firstBlockTest() {
        assertTrue(blockchain.firstBlock().verifyBlock());
        assertEquals(1, blockchain.length());
    }

    public void verifyChainTest() {
        // 1 block chain verification
        assertTrue(blockchain.verifyChain());

        // 2 blocks chain verification
        assertTrue(blockchain.addBlock(tranWithMiner2));
        assertTrue(blockchain.verifyChain());

        // 3 blocks chain verification
        assertTrue(blockchain.addBlock(tranWithMiner1));
        assertTrue(blockchain.verifyChain());

        blockchain.removeLastBlock();
        blockchain.removeLastBlock();

    }

    private void cloneTest() {
        Blockchain<MinedTransaction> newBlockchain = blockchain.clone();
        assertNotSame(newBlockchain, blockchain);
        assertNotSame(newBlockchain.getBlocks(), blockchain.getBlocks());
        assertEquals(newBlockchain, blockchain);

        newBlockchain = BlockchainFactory.getFirstInstance(tranWithMiner1);
        newBlockchain.addBlock(BlockFactory.getMinedInstance(newBlockchain.firstBlock(),
                tranWithMiner1));
        assertNotEquals(blockchain, newBlockchain);

    }

    private void addBlock() {
        Block<MinedTransaction> block = BlockFactory.getMinedInstance(blockchain.firstBlock(), tranWithMiner1);
        blockchain.addBlock(block);
        assertEquals(block, blockchain.getBlocks().elementAt(1));

        assertTrue(blockchain.addBlock(tranWithMiner2));
        assertEquals(tranWithMiner2, blockchain.getBlocks().elementAt(2).getData());

        blockchain.removeLastBlock();
        blockchain.removeLastBlock();
    }


    public void replaceChainTest() {
        // Replace chain with itself = success
        assertTrue(blockchain.replaceChain(blockchain));

        Blockchain<MinedTransaction> newBlockchain = BlockchainFactory.getFirstInstance(tranWithMiner1);

        // Replace chain with longer and 2 data having different id = success
        assertTrue(blockchain.addBlock(tranWithMiner1));
        assertTrue(newBlockchain.replaceChain(blockchain));

        // Replace chain with longer and 3 data having different id = success
        assertTrue(blockchain.addBlock(tranWithMiner2));
        assertTrue(newBlockchain.replaceChain(blockchain));

        // After replacing, the new chain should be equal to the old chain but not the same
        assertEquals(newBlockchain, blockchain);
        assertNotSame(newBlockchain, blockchain);
        assertEquals(newBlockchain.getBlocks(), blockchain.getBlocks());
        assertNotSame(newBlockchain.getBlocks(), blockchain.getBlocks());

        // Replace chain with same length = fail
        assertFalse(newBlockchain.replaceChain(blockchain));

        // Replace chain with less length = fail
        blockchain.removeLastBlock();
        assertFalse(newBlockchain.replaceChain(blockchain));

        // Different chains with different lengths are not equal
        assertNotEquals(newBlockchain, blockchain);
        assertNotEquals(newBlockchain.getBlocks(), blockchain.getBlocks());

        // Different chains with same lengths and same data are equal
        newBlockchain.removeLastBlock();
        assertEquals(newBlockchain, blockchain);
        assertEquals(newBlockchain.getBlocks(), blockchain.getBlocks());

        // Different chains with same lengths but different data are not equal
        blockchain.removeLastBlock();
        assertTrue(blockchain.addBlock(tranWithMiner2));
        System.out.println(blockchain);
        System.out.println(newBlockchain);
        assertNotEquals(newBlockchain, blockchain);
        assertNotEquals(newBlockchain.getBlocks(), blockchain.getBlocks());

        blockchain.removeLastBlock();
        blockchain.removeLastBlock();

    }
}