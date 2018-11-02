package com.atypon.gui;

import com.atypon.factory.UserFactory;
import com.atypon.ClientSocket;
import com.atypon.userAPI.User;

import javax.swing.*;

public class Login {
    private JPanel panelMain;
    private JTextField textFieldPort;
    private JButton buttonNew;
    private JButton buttonLoad;
    private JFrame thisFrame;

    private Login(JFrame thisFrame) {
        // Initialize components
        this.thisFrame = thisFrame;

        // Initialize JFrame
        initFrame();

        // Add listeners
        addListeners();
    }

    public static void runLogin() {
        JFrame loginFrame = new JFrame("Login");
        new Login(loginFrame);
    }

    private void addListeners() {
        buttonNew.addActionListener(e -> {
            String port = textFieldPort.getText();
            boolean isPort = ClientSocket.isPort(port);
            if (!isPort) {
                JOptionPane.showMessageDialog(null, "Please enter a valid port number!");
                return;
            }
            // create new User and move to Window
            String alias = JOptionPane.showInputDialog("Enter an Alias.");
            // Cancel returns null, so don't log in.
            if (alias == null)
                return;
            // If alias is empty, show an error message and return.
            if (alias.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Alias cannot be empty!");
                return;
            }

            // Confirm login which may overwrite older user with same port number.
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null,
                    "this will overwrite any previous user with the same port number!"))
                return;
            User user = UserFactory.getInstance(alias, Integer.parseInt(port));
            runWindow(user);
        });

        buttonLoad.addActionListener(e -> {
            String port = textFieldPort.getText();
            boolean isPort = ClientSocket.isPort(port);
            if (!isPort) {
                JOptionPane.showMessageDialog(null, "Please enter a valid port number!");
                return;
            }
            // read User from file if exists and move to Window
            User user = UserFactory.readUser(port);

            if (user == null) {
                JOptionPane.showMessageDialog(null, "Couldn't read the object linked to this port!");
            } else {
                runWindow(user);
            }
        });
    }

    private void runWindow(User user) {
        Window.runWindow(user, thisFrame);
    }

    private void initFrame() {
        this.thisFrame.setContentPane(panelMain);
        this.thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.thisFrame.pack();
        this.thisFrame.setLocationRelativeTo(null);
        this.thisFrame.setResizable(false);
        this.thisFrame.setVisible(true);
    }


}
