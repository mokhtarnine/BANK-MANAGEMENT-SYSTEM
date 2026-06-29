package com.bank;

import javax.swing.SwingUtilities;

import com.bank.controller.BankController;
import com.bank.gui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Application entry point.
 *
 * Startup order:
 * 1. Apply FlatLaf style.
 * 2. Create one BankController.
 * 3. Show LoginFrame.
 * 4. LoginFrame opens MainFrame after successful authentication.
 */
public class Main {

    public static void main(String[] args) {
        // Apply FlatLaf once before creating any Swing component.
        FlatLightLaf.setup();
        
        SwingUtilities.invokeLater(() -> {
            BankController controller = new BankController();

            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}
