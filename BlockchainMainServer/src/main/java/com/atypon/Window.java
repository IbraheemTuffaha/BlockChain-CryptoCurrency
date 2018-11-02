package com.atypon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JFrame API interface for the Blockchain server that holds
 * the addresses for all the nodes in the peer2peer network.
 */
public class Window extends JFrame {
    // The port which the server will listen on.
    private final static int PORT = 2000;
    // The server that will be run.
    private Server server;

    /**
     * Initializes the interface, sets it to visible, sets its
     * height and width and sets the default close operation.
     */
    public Window() {
        super("Window");
        initComponents();
        addComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void runWindow() {
        new Window();
    }

    /**
     * Checks whether the server is online or offline and
     * sets {@link #textFieldFeedback} text accordingly.
     */
    private void checkServerState() {
        if (server != null && server.isRunning())
            textFieldFeedback.setText("Server is online.");
        else
            textFieldFeedback.setText("Server is offline.");
    }

    /**
     * Add a message to the log.
     *
     * @param message The message to be added.
     */
    public void updateLog(String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:");
        message = formatter.format(new Date()) + message;
        textAreaLog.append(message);
        System.out.println(message);
        scrollToEnd();
    }

///////////////// GUI code.

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;
    private final static int TOP_PANELS_HEIGHT = 35;
    private JPanel panelMain;
    private JPanel panelFeedBack;
    private JPanel panelButtons;
    private JPanel panelLog;

    private JLabel labelFeedback;
    private JTextField textFieldFeedback;
    private JButton buttonStart;
    private JButton buttonStop;
    private JButton buttonReset;
    private JTextArea textAreaLog;
    private JScrollPane scrollPaneLog;


    /**
     * Initialize components of the interface.
     */
    private void initComponents() {
        panelMain = new JPanel();


        panelFeedBack = new JPanel(new FlowLayout());
        panelButtons = new JPanel(new FlowLayout());
        panelLog = new JPanel(new FlowLayout());

        labelFeedback = new JLabel("Server Status");

        textFieldFeedback = new JTextField();
        textFieldFeedback.setEditable(false);

        buttonStart = new JButton("Start Server");
        buttonStart.addActionListener(new ActionListener_buttonStart());

        buttonStop = new JButton("Stop Server");
        buttonStop.addActionListener(new ActionListener_buttonStop());

        buttonReset = new JButton("Reset Database");
        buttonReset.addActionListener(new ActionListener_buttonReset());

        textAreaLog = new JTextArea();

        scrollPaneLog = new JScrollPane(textAreaLog);
        scrollPaneLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        checkServerState();
    }

    /**
     * Align components of the interface.
     */
    private void addComponents() {
        this.setContentPane(panelMain);
        this.setResizable(false);

        panelMain.add(panelFeedBack);
        panelMain.add(panelButtons);
        panelMain.add(panelLog);
        panelFeedBack.add(labelFeedback);
        panelFeedBack.add(textFieldFeedback);
        panelFeedBack.setPreferredSize(new Dimension(WIDTH, TOP_PANELS_HEIGHT));
        panelButtons.add(buttonStart);
        panelButtons.add(buttonStop);
        panelButtons.add(buttonReset);
        panelButtons.setPreferredSize(new Dimension(WIDTH, TOP_PANELS_HEIGHT));
        panelLog.add(scrollPaneLog);

        textFieldFeedback.setPreferredSize(new Dimension(125, 25));
        scrollPaneLog.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT - 4 * TOP_PANELS_HEIGHT));
    }

    /**
     * Action listener for {@link #buttonStart} to run the server when
     * clicked if server is offline.
     */
    class ActionListener_buttonStart implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (server != null && server.isRunning())
                return;
            // Create and run the server in a separate thread.
            server = new Server(PORT, Window.this);
            server.start();
            checkServerState();
        }
    }

    /**
     * Action listener for {@link #buttonStop} to stop the server when
     * clicked if server is online.
     */
    class ActionListener_buttonStop implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (server != null) {
                server.stopServerSocket();
                server.interrupt();
            }
            checkServerState();
        }
    }

    /**
     * Action listener for {@link #buttonStart} to run the server when
     * clicked if server is offline.
     */
    class ActionListener_buttonReset implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (server == null || !server.isRunning())
                return;
            server.resetDatabase();
            server.printOnWindow("Database was reset.");
        }
    }

    /**
     * Scroll the scroll pane to the end as the log is being added to the text area.
     */
    private void scrollToEnd() {
        JScrollBar scrollBar = scrollPaneLog.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

}
