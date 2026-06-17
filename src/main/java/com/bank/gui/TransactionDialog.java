package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bank.controller.BankController;
import com.bank.exception.AccountClosedException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public class TransactionDialog extends JDialog {

    private final BankController controller;
    private final String transactionType;
    private final String accountNumber;
    private final AccountDetailPanel accountDetailPanel;
    private final MainFrame mainFrame;

    private final JTextField amountField;
    private final JTextField targetAccountField;

    public TransactionDialog(
            JFrame parent,
            BankController controller,
            String transactionType,
            String accountNumber,
            AccountDetailPanel accountDetailPanel,
            MainFrame mainFrame
    ) {
        super(parent, transactionType, true);

        this.controller = controller;
        this.transactionType = transactionType;
        this.accountNumber = accountNumber;
        this.accountDetailPanel = accountDetailPanel;
        this.mainFrame = mainFrame;

        this.amountField = new JTextField();
        this.targetAccountField = new JTextField();

        configureDialog();
        buildLayout();
    }

    private void configureDialog() {
        setSize(400, 220);
        setLocationRelativeTo(getParent());
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.add(new JLabel("Account:"));
        formPanel.add(new JLabel(accountNumber));

        formPanel.add(new JLabel("Amount:"));
        formPanel.add(amountField);

        if (transactionType.equals("TRANSFER")) {
            formPanel.add(new JLabel("Target Account:"));
            formPanel.add(targetAccountField);
        }

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> performTransaction());

        add(formPanel, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);
    }

    private void performTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText());

            if (transactionType.equals("DEPOSIT")) {
                controller.deposit(
                        accountNumber,
                        amount
                );

            } else if (transactionType.equals("WITHDRAW")) {
                controller.withdraw(
                        accountNumber,
                        amount
                );

            } else if (transactionType.equals("TRANSFER")) {
                controller.transfer(
                        accountNumber,
                        targetAccountField.getText(),
                        amount
                );
            }

            mainFrame.refreshAllPanels();

            JOptionPane.showMessageDialog(
                    this,
                    "Transaction completed successfully."
            );

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Amount must be a valid number."
            );

        } catch (InvalidAmountException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid amount: " + e.getAmount()
            );

        } catch (InsufficientFundsException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Insufficient funds. Available: " + e.getAvailable()
            );

        } catch (AccountClosedException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Account is closed: " + e.getAccountNumber()
            );

        } catch (BankException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }
}