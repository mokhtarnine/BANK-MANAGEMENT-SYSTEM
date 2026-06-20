package com.bank;

import javax.swing.SwingUtilities;

import com.bank.controller.BankController;
import com.bank.gui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {

    public static void main(String[] args) {
        // Apply FlatLaf once before creating any swing somponent.
        FlatLightLaf.setup();
        
        SwingUtilities.invokeLater(() -> {
            BankController controller = new BankController();

            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}