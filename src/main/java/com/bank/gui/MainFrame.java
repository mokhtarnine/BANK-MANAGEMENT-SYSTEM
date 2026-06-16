package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bank.controller.BankController;

public class MainFrame extends JFrame {

    private final BankController controller;

    private final JTabbedPane tabs;
    private final JPanel dashboardPanel;
    private final JPanel customerListPanel;
    private final JPanel alertLogPanel;
    private final AccountDetailPanel accountDetailPanel;

    public MainFrame() {
        this.controller = new BankController();

        this.tabs = new JTabbedPane();

        this.dashboardPanel = new DashboardPanel(controller);
        this.accountDetailPanel = new AccountDetailPanel(controller);
        this.customerListPanel = new CustomerListPanel(controller, accountDetailPanel);
        this.alertLogPanel = new AlertLogPanel();
        

        configureFrame();
        buildLayout();
        configureCloseAction();
    }

    private void configureFrame() {
        setTitle("Bank Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Customers", customerListPanel);
        tabs.addTab("Accounts", accountDetailPanel);
        tabs.addTab("Alerts", alertLogPanel);
        

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title, JLabel.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private void configureCloseAction() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.shutdownThreads();
                dispose();
                System.exit(0);
            }
        });
    }
}