package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.bank.controller.BankController;
import com.bank.model.Customer;
import com.bank.model.Transaction;

public class AccountDetailPanel extends JPanel {

    private final BankController controller;
    private Customer customer;

    private final JLabel customerLabel;
    private final JTextField accountSearchField;
    private final JComboBox<String> accountTypeFilter;
    private final JTable accountTable;
    private final DefaultTableModel accountTableModel;
    private final JTable transactionTable;
    private final DefaultTableModel transactionTableModel;
    private final MainFrame mainFrame;

    public AccountDetailPanel(BankController controller,MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        this.customerLabel = new JLabel("No customer selected");
        this.accountSearchField = new JTextField();
        this.accountTypeFilter = new JComboBox<>(
                new String[]{"ALL", "CHECKING", "SAVINGS"}
        );

        this.accountTableModel = new DefaultTableModel(
                new Object[]{"Account Number", "Type", "Balance", "Closed"},
                0
        );

        this.accountTable = new JTable(accountTableModel);
        this.transactionTableModel = new DefaultTableModel(
                new Object[]{"ID", "Type", "Amount", "Time", "Balance After", "Employee"},
                0
        );
        this.transactionTable = new JTable(transactionTableModel);

        buildLayout();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(customerLabel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new GridLayout(1, 6, 5, 5));

        filterPanel.add(new JLabel("Account Number:"));
        filterPanel.add(accountSearchField);
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(accountTypeFilter);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> refreshAccounts());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAccountFilters());

        filterPanel.add(searchButton);
        filterPanel.add(clearButton);

        /*
         * Pressing Enter in the account-number field applies the filters
         * without requiring the user to click the Search button.
         */
        accountSearchField.addActionListener(e -> refreshAccounts());
        accountTypeFilter.addActionListener(e -> refreshAccounts());

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        centerPanel.add(new JScrollPane(accountTable));
        centerPanel.add(new JScrollPane(transactionTable));

        add(centerPanel, BorderLayout.CENTER);

        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshTransactionHistory();
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 5, 5));

        JButton openAccountButton = new JButton("Open Account");
        openAccountButton.addActionListener(e -> openAccount());

        JButton closeAccountButton = new JButton("Close Account");
        closeAccountButton.addActionListener(e -> closeSelectedAccount());

        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> openTransactionDialog("DEPOSIT"));

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> openTransactionDialog("WITHDRAW"));

        JButton transferButton = new JButton("Transfer");
        transferButton.addActionListener(e -> openTransactionDialog("TRANSFER"));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAccounts());

        buttonPanel.add(openAccountButton);
        buttonPanel.add(closeAccountButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(transferButton);
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

        Object[][] rows = controller.getCustomerAccountTableData(
                customer.getId(),
                accountSearchField.getText(),
                (String) accountTypeFilter.getSelectedItem()
        );

        for (Object[] row : rows) {
            accountTableModel.addRow(row);
        }

        refreshTransactionHistory();
    }

    /**
     * Restores the account table to its unfiltered state.
     */
    private void clearAccountFilters() {
        accountSearchField.setText("");
        accountTypeFilter.setSelectedItem("ALL");
        refreshAccounts();
    }

    private void refreshTransactionHistory() {
        transactionTableModel.setRowCount(0);

        String accountNumber = getSelectedAccountNumberWithoutMessage();

        if (accountNumber == null) {
            return;
        }

        for (Transaction transaction : controller.getTransactionHistory(accountNumber)) {
            String employeeName = "";

            if (transaction.getPerformedBy() != null) {
                employeeName = transaction.getPerformedBy().getFullName();
            }

            transactionTableModel.addRow(new Object[]{
                    transaction.getTransactionId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    transaction.getBalanceAfter(),
                    employeeName
            });
        }
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

                mainFrame.refreshAllPanels();

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
        String accountNumber = getSelectedAccountNumber();

        if (accountNumber == null) {
            return;
        }
        try {
            controller.closeAccount(accountNumber);
            mainFrame.refreshAllPanels();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }

    private String getSelectedAccountNumber() {
        int selectedRow = accountTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an account first."
            );
            return null;
        }

        return accountTableModel
                .getValueAt(selectedRow, 0)
                .toString();
    }

    private String getSelectedAccountNumberWithoutMessage() {
        int selectedRow = accountTable.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        return accountTableModel
                .getValueAt(selectedRow, 0)
                .toString();
    }

    private void openTransactionDialog(String transactionType) {
        String accountNumber = getSelectedAccountNumber();

        if (accountNumber == null) {
            return;
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        TransactionDialog dialog = new TransactionDialog(
                parentFrame,
                controller,
                transactionType,
                accountNumber,
                this,
                mainFrame
        );

        dialog.setVisible(true);
    }


}
