package com.atypon.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ShowTable {
    private JScrollPane scrollPane;
    private JPanel panelMain;
    private JTable tableUsers;
    private JPanel scrollPanel;

    private JFrame thisFrame;
    private JFrame prvFrame;
    private Object[] columnNames;
    private Object[][] data;

    private ShowTable(JFrame thisFrame, JFrame prvFrame, Object[] columnNames, Object[][] data) {
        this.thisFrame = thisFrame;
        this.prvFrame = prvFrame;
        this.columnNames = columnNames;
        this.data = data;

        // Initialize JFrame
        initFrame();

        // Add listeners
        addListeners();

    }

    public static void runShowTable(JFrame prvFrame, Object[] columnNames, Object[][] data, String tableName) {
        prvFrame.setVisible(false);
        JFrame showFrame = new JFrame(tableName);
        new ShowTable(showFrame, prvFrame, columnNames, data);
    }

    private void initFrame() {
        this.thisFrame.setContentPane(panelMain);
        this.thisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.thisFrame.pack();
        this.thisFrame.setLocationRelativeTo(null);
        this.thisFrame.setResizable(false);
        this.thisFrame.setVisible(true);


        DefaultTableModel model = new DefaultTableModel(data, columnNames) {

            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };

        tableUsers.setModel(model);
        tableUsers.setRowHeight(50);
    }

    private void addListeners() {
        thisFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                ShowTable.this.prvFrame.setVisible(true);
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