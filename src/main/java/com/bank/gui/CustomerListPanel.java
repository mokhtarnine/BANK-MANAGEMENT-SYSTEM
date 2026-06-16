package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.bank.controller.BankController;
import com.bank.model.Customer;

public class CustomerListPanel extends JPanel {

    private final BankController controller;
    private final AccountDetailPanel accountDetailPanel;

    private final JTextField searchField;
    private final JTable customerTable;
    private final DefaultTableModel tableModel;

    public CustomerListPanel(BankController controller,AccountDetailPanel accountDetailPanel) {
        this.controller = controller;
        this.accountDetailPanel = accountDetailPanel;
        this.searchField = new JTextField();

        this.tableModel = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "Username", "Email", "Accounts"},
                0
        );

        this.customerTable = new JTable(tableModel);

        buildLayout();
        refreshCustomers();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(
                new JLabel("Search: "),
                BorderLayout.WEST
        );

        topPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshCustomers());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));

        JButton addButton = new JButton("Add Customer");
        addButton.addActionListener(e -> addCustomer());

        JButton viewAccountsButton = new JButton("View Accounts");
        viewAccountsButton.addActionListener(e -> viewSelectedCustomerAccounts());

        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(viewAccountsButton);

        topPanel.add(
                buttonPanel,
                BorderLayout.EAST
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );

        add(
                new JScrollPane(customerTable),
                BorderLayout.CENTER
        );
    }

    public void refreshCustomers() {
        Collection<Customer> customers = controller.getAllCustomers();
        loadCustomers(customers);
    }

    private void searchCustomers() {
        String keyword = searchField.getText();

        if (keyword.trim().isEmpty()) {
            refreshCustomers();
            return;
        }

        List<Customer> customers = controller.searchCustomers(keyword);
        loadCustomers(customers);
    }

    private void loadCustomers(Collection<Customer> customers) {
        tableModel.setRowCount(0);

        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                    customer.getId(),
                    customer.getFullName(),
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getAccounts().size()
            });
        }
    }

    private void addCustomer() {
        JTextField idField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField emailField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Add Customer",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            controller.createCustomer(
                    idField.getText(),
                    fullNameField.getText(),
                    usernameField.getText(),
                    passwordField.getText(),
                    emailField.getText()
            );

            refreshCustomers();
        }
    }
    private void viewSelectedCustomerAccounts() {
        int selectedRow = customerTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a customer first."
            );
            return;
        }

        String customerId = tableModel
                .getValueAt(selectedRow, 0)
                .toString();

        Customer selectedCustomer = null;

        for (Customer customer : controller.getAllCustomers()) {
            if (customer.getId().equals(customerId)) {
                selectedCustomer = customer;
                break;
            }
        }

        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Customer not found."
            );
            return;
        }

        accountDetailPanel.setCustomer(selectedCustomer);
    }
}