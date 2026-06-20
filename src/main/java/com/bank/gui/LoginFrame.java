package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.bank.controller.BankController;
import com.bank.gui.style.UIStyle;

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
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UIStyle.BACKGROUND_COLOR);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        UIStyle.stylePanel(contentPanel);

        JLabel titleLabel = new JLabel(
                "Employee Login",
                JLabel.CENTER
        );
        UIStyle.styleTitle(titleLabel);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        usernameField.setFont(UIStyle.NORMAL_FONT);
        passwordField.setFont(UIStyle.NORMAL_FONT);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        UIStyle.styleButton(loginButton);
        loginButton.addActionListener(e -> login());

        /*
         * The default button is activated when Enter is pressed anywhere
         * inside the login window.
         */
        getRootPane().setDefaultButton(loginButton);

        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(loginButton, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
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
