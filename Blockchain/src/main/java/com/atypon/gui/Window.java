package com.atypon.gui;

import com.atypon.ClientSocket;
import com.atypon.blockchain.Block;
import com.atypon.blockchain.Blockchain;
import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;
import com.atypon.factory.UserFactory;
import com.atypon.userAPI.User;
import com.atypon.utility.BitManipulation;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

public class Window {
    private JFrame thisFrame;
    private JFrame prvFrame;
    private JPanel panelMain;
    private JTextField textFieldAlias;
    private JTextArea textAreaLog;
    private JTextField textFieldBalance;
    private JTextField textFieldPublicKey;
    private JButton buttonToggleMining;
    private JButton buttonNewTransaction;
    private JButton buttonNewBlockchain;
    private JButton buttonConnectNetwork;
    private JButton buttonShowUsers;
    private JButton buttonShowBlockchain;
    private JScrollPane scrollPaneLog;
    private JButton buttonTransactionPool;
    private JButton startMiningButton;

    private User user;

    private Window(JFrame thisFrame, JFrame prvFrame, User user) {
        this.thisFrame = thisFrame;
        this.prvFrame = prvFrame;
        this.user = user;
        user.setWindow(this);
        user.runListener();

        // Initialize JFrame
        initFrame();

        // Initialize Components
        initComponents();

        // Add listeners
        addListeners();

    }

    private void initComponents() {
        textFieldAlias.setText(user.getAlias());
        buttonToggleMining.setText("Toggle Mining " + (!user.isMiningOn() ? "On" : "Off"));
        setNetWorth();
        setPublicKey();
    }

    private void setNetWorth() {
        textFieldBalance.setText(user.getNetWorth(user.getPublicKey()).toString());
    }

    private void setPublicKey() {
        textFieldPublicKey.setText(BitManipulation.byteArrayToString(user.getPublicKey().getEncoded()).substring(0, 50) + " (first 50 characters)");
    }

    private void initFrame() {
        this.thisFrame.setContentPane(panelMain);
        this.thisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.thisFrame.pack();
        this.thisFrame.setLocationRelativeTo(null);
        this.thisFrame.setResizable(false);
        this.thisFrame.setVisible(true);
    }

    private void addListeners() {
        thisFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                UserFactory.writeUser(user.getPort() + "", user);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                user.getClient().stopRunning();
                UserFactory.writeUser(user.getPort() + "", user);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                Window.this.prvFrame.setVisible(true);
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        buttonConnectNetwork.addActionListener(e -> {
            updateLog("Connecting to server..");
            if (user.getClient().getClientsFromServer())
                updateLog("Users loading complete.");
            else
                updateLog("Couldn't load users.");

        });
        buttonNewBlockchain.addActionListener(e -> {
            updateLog("Creating a new blockchain..");
            user.createChain();
            updateLog("new blockchain created.");
            setNetWorth();
        });
        buttonToggleMining.addActionListener(e -> {
            updateLog("Toggling mining status..");
            System.out.println(user.isMiningOn());
            user.setMiningOn(!user.isMiningOn());
            buttonToggleMining.setText("Toggle Mining " + (!user.isMiningOn() ? "On" : "Off"));
            updateLog("Mining status changed to " + (user.isMiningOn() ? "'On'" : "'Off'") + ".");
        });
        buttonNewTransaction.addActionListener(e -> NewTransaction.runNewTransaction(thisFrame, user));
        buttonShowUsers.addActionListener(e -> {

            Vector<ClientSocket> clients = user.getClients();

            Object[] columnNames = {"Alias", "Public Key", "Balance"};
            Object[][] data = new Object[clients.size()][3];

            for (int i = 0; i < clients.size(); ++i) {
                data[i][0] = clients.elementAt(i).getAlias();
                data[i][1] = clients.elementAt(i).getPublicKeyString();
                data[i][2] = user.getNetWorth(clients.elementAt(i).getPublicKey());
            }
            runShow(columnNames, data, "Users");
        });

        buttonShowBlockchain.addActionListener(e -> {
            Blockchain<MinedTransaction> blockchain = user.getBlockchain();
            Object[] columnNames = {"Previous Hash", "Current Hash", "Nonce",
                    "ID", "Sender", "Receiver", "Amount", "Is Signed?",
                    "Miner", "Mining Fees", "Mining Reward"};

            Object[][] data = new Object[blockchain.length()][11];
            int index = 0;
            for (Block<MinedTransaction> block : blockchain.getBlocks()) {
                MinedTransaction transaction = block.getData();
                data[index][0] = block.getPrvHash();
                data[index][1] = block.getHash();
                data[index][2] = block.getNonce();
                data[index][3] = transaction.getId();
                data[index][4] = user.getAlias(transaction.getSenderPublicKey());
                data[index][5] = user.getAlias(transaction.getReceiverPublicKey());
                data[index][6] = transaction.getAmount();
                data[index][7] = transaction.verifySignature();
                data[index][8] = user.getAlias(transaction.getMinerPublicKey());
                data[index][9] = transaction.getMiningFee();
                data[index][10] = transaction.getMiningReward();
                ++index;
            }
            runShow(columnNames, data, "Blockchain");
        });

        buttonTransactionPool.addActionListener(e -> {
            Object[] columnNames = {"ID", "Sender", "Receiver", "Amount", "Is Signed?"};
            Object[][] data;
            synchronized (user.getTransactionPool()) {
                data = new Object[user.getTransactionPool().size()][5];
                int index = 0;
                for (Transaction transaction : user.getTransactionPool()) {
                    data[index][0] = transaction.getId();
                    data[index][1] = user.getAlias(transaction.getSenderPublicKey());
                    data[index][2] = user.getAlias(transaction.getReceiverPublicKey());
                    data[index][3] = transaction.getAmount();
                    data[index][4] = transaction.verifySignature();
                    ++index;
                }
            }

            runShow(columnNames, data, "Blockchain");
        });

        startMiningButton.addActionListener(e -> {
            if (isRunning)
                return;
            isRunning = true;
            Thread thread = new Thread(() -> {
                if (user.mine())
                    JOptionPane.showMessageDialog(null, "Successfully mined a block!");
                else
                    JOptionPane.showMessageDialog(null, "Mining failed!");
                isRunning = false;
            });

            thread.start();
        });
    }

    private boolean isRunning = false;

    private void runShow(Object[] columnNames, Object[][] data, String tableName) {
        ShowTable.runShowTable(thisFrame, columnNames, data, tableName);
    }

    public static void runWindow(User user, JFrame prvFrame) {
        prvFrame.setVisible(false);
        JFrame windowFrame = new JFrame("Client");
        new Window(windowFrame, prvFrame, user);
    }

    /**
     * Add a message to the log.
     *
     * @param message The message to be added.
     */
    public void updateLog(String message) {
        textAreaLog.append(message + '\n');

        setNetWorth();
        scrollToEnd();
    }

    /**
     * Scroll the scroll pane to the end as the log is being added to the text area.
     */
    private void scrollToEnd() {
        JScrollBar scrollBar = scrollPaneLog.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

}
