package com.atypon.gui;

import com.atypon.ClientSocket;
import com.atypon.userAPI.User;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;
import java.util.Vector;

public class NewTransaction {
    private JPanel panelMain;
    private JComboBox<String> comboBoxUsers;
    private JTextField textFieldAmount;
    private JButton buttonTransaction;

    private JFrame thisFrame;
    private JFrame prvFrame;
    private User user;
    private Vector<ClientSocket> clients;

    private NewTransaction(JFrame thisFrame, JFrame prvFrame, User user) {
        this.thisFrame = thisFrame;
        this.prvFrame = prvFrame;
        this.user = user;
        synchronized (user.getClients()) {
            clients = user.getClients();
        }

        // Initialize JFrame
        initFrame();

        // Add listeners
        addListeners();

    }

    public static void runNewTransaction(JFrame prvFrame, User clients) {
        prvFrame.setVisible(false);
        JFrame transactionFrame = new JFrame("New Transaction");
        new NewTransaction(transactionFrame, prvFrame, clients);
    }

    private void initFrame() {
        this.thisFrame.setContentPane(panelMain);
        this.thisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.thisFrame.pack();
        this.thisFrame.setLocationRelativeTo(null);
        this.thisFrame.setResizable(false);
        this.thisFrame.setVisible(true);


        for (ClientSocket clientSocket : clients)
            comboBoxUsers.addItem(clientSocket.getAlias() + " -> " +
                    clientSocket.getPublicKeyString().substring(0, 70) + " (first 70 characters)");
    }

    private void addListeners() {

        buttonTransaction.addActionListener(e -> {
            try {
                BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(textFieldAmount.getText()));
                int index = comboBoxUsers.getSelectedIndex();
                if (amount.compareTo(BigDecimal.ZERO) > 0 && user.makeTransaction(clients.elementAt(index).getPublicKey(), amount)) {
                    JOptionPane.showMessageDialog(null, "Successfully added the transaction to the pool.");
                    thisFrame.dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
                } else {
                    JOptionPane.showMessageDialog(null, "Couldn't add the transaction to the pool.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        thisFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                NewTransaction.this.prvFrame.setVisible(true);
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
    }
}
