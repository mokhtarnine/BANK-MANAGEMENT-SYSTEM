package com.bank.gui;

import com.bank.controller.BankController;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {

    private final BankController controller;

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame(BankController controller) {
        this.controller = controller;

        this.usernameField = new JTextField();
        this.passwordField = new JPasswordField();

        configureFrame();
        buildLayout();
    }

    private void configureFrame() {
        setTitle("Bank Management System - Login");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(
                "Employee Login",
                JLabel.CENTER
        );

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        boolean success = controller.login(username, password);

        if (success) {
            MainFrame mainFrame = new MainFrame(controller);
            mainFrame.setVisible(true);

            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password."
            );
        }
    }
}