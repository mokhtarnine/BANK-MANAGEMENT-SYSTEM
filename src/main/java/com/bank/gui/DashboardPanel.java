package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bank.controller.BankController;
import com.bank.gui.style.UIStyle;

/**
 * Dashboard tab that shows summary numbers for the whole bank.
 *
 * The values are calculated through BankController, then displayed in labels.
 */
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
        UIStyle.stylePanel(this);

        JLabel titleLabel = new JLabel(
                "Dashboard",
                JLabel.CENTER
        );
        UIStyle.styleTitle(titleLabel);

        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        statsPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        customersValueLabel.setFont(UIStyle.TITLE_FONT);
        customersValueLabel.setForeground(UIStyle.TITLE_COLOR);

        accountsValueLabel.setFont(UIStyle.TITLE_FONT);
        accountsValueLabel.setForeground(UIStyle.TITLE_COLOR);

        balanceValueLabel.setFont(UIStyle.TITLE_FONT);
        balanceValueLabel.setForeground(UIStyle.TITLE_COLOR);



        statsPanel.add(new JLabel("Total Customers:"));
        statsPanel.add(customersValueLabel);

        statsPanel.add(new JLabel("Total Accounts:"));
        statsPanel.add(accountsValueLabel);

        statsPanel.add(new JLabel("Total Balance:"));
        statsPanel.add(balanceValueLabel);


        JButton refreshButton = new JButton("Refresh");
        UIStyle.styleButton(refreshButton);

        refreshButton.addActionListener(e -> refreshDashboard());

        add(titleLabel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);
    }

    public void refreshDashboard() {
        // Read fresh totals every time the dashboard is opened or refreshed.
        int totalCustomers = controller.getTotalCustomers();
        int totalAccounts = controller.getTotalAccounts();
        double totalBalance = controller.getTotalBalance();

        customersValueLabel.setText(String.valueOf(totalCustomers));
        accountsValueLabel.setText(String.valueOf(totalAccounts));
        balanceValueLabel.setText(String.format("%.2f", totalBalance));
    }
}
