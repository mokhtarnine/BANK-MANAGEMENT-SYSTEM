package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.bank.controller.BankController;
import com.bank.model.Account;
import com.bank.model.Customer;

public class AccountDetailPanel extends JPanel {

    private final BankController controller;
    private Customer customer;

    private final JLabel customerLabel;
    private final JTable accountTable;
    private final DefaultTableModel accountTableModel;

    public AccountDetailPanel(BankController controller) {
        this.controller = controller;

        this.customerLabel = new JLabel("No customer selected");

        this.accountTableModel = new DefaultTableModel(
                new Object[]{"Account Number", "Type", "Balance", "Closed"},
                0
        );

        this.accountTable = new JTable(accountTableModel);

        buildLayout();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        add(customerLabel, BorderLayout.NORTH);

        add(
                new JScrollPane(accountTable),
                BorderLayout.CENTER
        );

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));

        JButton openAccountButton = new JButton("Open Account");
        openAccountButton.addActionListener(e -> openAccount());

        JButton closeAccountButton = new JButton("Close Account");
        closeAccountButton.addActionListener(e -> closeSelectedAccount());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAccounts());

        buttonPanel.add(openAccountButton);
        buttonPanel.add(closeAccountButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;

        if (customer == null) {
            customerLabel.setText("No customer selected");
        } else {
            customerLabel.setText(
                    "Customer: "
                            + customer.getFullName()
                            + " | ID: "
                            + customer.getId()
            );
        }

        refreshAccounts();
    }

    public void refreshAccounts() {
        accountTableModel.setRowCount(0);

        if (customer == null) {
            return;
        }

        for (Account account : customer.getAccounts()) {
            accountTableModel.addRow(new Object[]{
                    account.getAccountNumber(),
                    getAccountType(account),
                    account.getBalance(),
                    account.isClosed()
            });
        }
    }

    private String getAccountType(Account account) {
        String className = account.getClass().getSimpleName();

        if (className.equals("CheckingAccount")) {
            return "CHECKING";
        }

        if (className.equals("SavingsAccount")) {
            return "SAVINGS";
        }

        return className;
    }

    private void openAccount() {
        if (customer == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a customer first."
            );
            return;
        }

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"CHECKING", "SAVINGS"}
        );

        JTextField initialBalanceField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        formPanel.add(new JLabel("Account Type:"));
        formPanel.add(typeBox);

        formPanel.add(new JLabel("Initial Balance:"));
        formPanel.add(initialBalanceField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Open Account",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                double initialBalance = Double.parseDouble(
                        initialBalanceField.getText()
                );

                controller.openAccount(
                        customer.getId(),
                        (String) typeBox.getSelectedItem(),
                        initialBalance
                );

                refreshAccounts();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Initial balance must be a valid number."
                );

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                        this,
                        e.getMessage()
                );
            }
        }
    }

    private void closeSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an account first."
            );
            return;
        }

        String accountNumber = accountTableModel
                .getValueAt(selectedRow, 0)
                .toString();

        try {
            controller.closeAccount(accountNumber);
            refreshAccounts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }
}