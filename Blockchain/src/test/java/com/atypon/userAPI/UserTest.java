package com.atypon.userAPI;

import com.atypon.factory.UserFactory;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void runMultipleUsersTest() throws InterruptedException {
        for (int i = 0; i <= 15; ++i)
            runTest(i);
    }

    private void runTest(int n) throws InterruptedException {
        User user1 = UserFactory.getInstance("Mohammad", 1111);
        //user1.setMiningOn(true);
        user1.runListener();

        User user3 = UserFactory.getInstance("Sarah", 3333);
        user3.setMiningOn(true);
        user3.runListener();

        User user2 = UserFactory.getInstance("Ahmad", 2222);
        user2.setMiningOn(true);
        user2.runListener();

        user1.getClient().addClient(user2.getClientSocket());
        user1.getClient().addClient(user3.getClientSocket());
        user2.getClient().addClient(user1.getClientSocket());
        user2.getClient().addClient(user3.getClientSocket());
        user3.getClient().addClient(user1.getClientSocket());
        user3.getClient().addClient(user2.getClientSocket());

        user2.createChain();
        BigDecimal amount = BigDecimal.valueOf(0.001);
        BigDecimal account = amount.multiply(BigDecimal.ONE.subtract(User.FEES_PERCENTAGE)).multiply(BigDecimal.valueOf(n));
        for (int i = 0; i < n; ++i)
            user2.makeTransaction(user1.getPublicKey(), amount);

        // Tested on First block 12 zeros, rest 6 zeros, if it fails then it needs more time.
        Thread.sleep(2000 + n * 250);

        checkUser(user1, account, user1, n);
        checkUser(user2, account, user1, n);
        checkUser(user3, account, user1, n);
    }

    private void checkUser(User user, BigDecimal account, User user1, int n) {
        user.stopListener();

        System.out.println(user.getNetWorth(user1.getPublicKey()));
        System.out.println(account);

        assertEquals(0, user.getNetWorth(user1.getPublicKey()).compareTo(account));
        assertTrue(User.verifyChain(user.getBlockchain()));
        assertEquals(n + 1, user.getBlockchain().length());

        BigDecimal total = BigDecimal.ZERO;
        total = total.add(user.getNetWorth(user.getClients().elementAt(0).getPublicKey()));
        total = total.add(user.getNetWorth(user.getClients().elementAt(1).getPublicKey()));
        total = total.add(user.getNetWorth(user.getClients().elementAt(2).getPublicKey()));

        BigDecimal actualTotal = BigDecimal.ZERO;
        BigDecimal reward = User.INIT_REWARD;
        for (int i = 0; i < user.getBlockchain().length(); ++i) {
            if (i == 0)
                actualTotal = actualTotal.add(User.CREATOR_STARTING_BALANCE);
            else {
                if (i % User.NUMBER_OF_BLOCKS_FOR_REDUCTION == 0)
                    reward = reward.multiply(BigDecimal.valueOf(0.5));
                actualTotal = actualTotal.add(reward);
            }
        }

        assertEquals(0, actualTotal.compareTo(total));
    }
}