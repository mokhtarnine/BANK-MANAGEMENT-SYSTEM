package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bank.controller.BankController;

public class MainFrame extends JFrame {

    private final BankController controller;

    private final JTabbedPane tabs;
    private final JPanel dashboardPanel;
    private final JPanel customerListPanel;
    private final AlertLogPanel alertLogPanel;
    private final AccountDetailPanel accountDetailPanel;

    public MainFrame(BankController controller) {
        this.controller = controller;

        this.tabs = new JTabbedPane();

        this.dashboardPanel = new DashboardPanel(controller);
        this.accountDetailPanel = new AccountDetailPanel(controller,this);
        this.customerListPanel = new CustomerListPanel(controller, accountDetailPanel, this);
        this.alertLogPanel = new AlertLogPanel();
        
        registerAlertHandler();
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
        
        tabs.addChangeListener(e -> refreshSelectedTab());
        add(tabs, BorderLayout.CENTER);
    }

    private void refreshSelectedTab() {
        JPanel selectedPanel = (JPanel) tabs.getSelectedComponent();

        if (selectedPanel == dashboardPanel) {
            ((DashboardPanel) dashboardPanel).refreshDashboard();
        } else if (selectedPanel == customerListPanel) {
            ((CustomerListPanel) customerListPanel).refreshCustomers();
        } else if (selectedPanel == accountDetailPanel) {
            accountDetailPanel.refreshAccounts();
        }
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

    public void showAccountsTab() {
        tabs.setSelectedComponent(accountDetailPanel);
    }

    public void refreshAllPanels() {
        ((DashboardPanel) dashboardPanel).refreshDashboard();
        ((CustomerListPanel) customerListPanel).refreshCustomers();
        accountDetailPanel.refreshAccounts();
    }

    private void registerAlertHandler() {
        controller.setAlertHandler(message -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                alertLogPanel.addAlert(message);
                tabs.setSelectedComponent(alertLogPanel);
            });
        });
    }

}