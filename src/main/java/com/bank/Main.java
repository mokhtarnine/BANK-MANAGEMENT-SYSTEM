package com.bank;

import javax.swing.SwingUtilities;

import com.bank.controller.BankController;
import com.bank.gui.LoginFrame;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankController controller = new BankController();

            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}