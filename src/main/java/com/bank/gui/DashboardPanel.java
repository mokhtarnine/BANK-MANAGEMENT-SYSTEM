package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bank.controller.BankController;
import com.bank.model.Account;

public class DashboardPanel extends JPanel {

    private final BankController controller;

    private final JLabel customersValueLabel;
    private final JLabel accountsValueLabel;
    private final JLabel balanceValueLabel;

    public DashboardPanel(BankController controller) {
        this.controller = controller;

        this.customersValueLabel = new JLabel("0");
        this.accountsValueLabel = new JLabel("0");
        this.balanceValueLabel = new JLabel("0.00");

        buildLayout();
        refreshDashboard();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(
                "Dashboard",
                JLabel.CENTER
        );

        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        statsPanel.add(new JLabel("Total Customers:"));
        statsPanel.add(customersValueLabel);

        statsPanel.add(new JLabel("Total Accounts:"));
        statsPanel.add(accountsValueLabel);

        statsPanel.add(new JLabel("Total Balance:"));
        statsPanel.add(balanceValueLabel);

        JButton refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(e -> refreshDashboard());

        add(titleLabel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);
    }

    public void refreshDashboard() {
        int totalCustomers = controller.getAllCustomers().size();

        List<Account> accounts = controller.getAllAccounts();

        int totalAccounts = accounts.size();

        double totalBalance = 0;

        for (Account account : accounts) {
            totalBalance += account.getBalance();
        }

        customersValueLabel.setText(String.valueOf(totalCustomers));
        accountsValueLabel.setText(String.valueOf(totalAccounts));
        balanceValueLabel.setText(String.format("%.2f", totalBalance));
    }
}