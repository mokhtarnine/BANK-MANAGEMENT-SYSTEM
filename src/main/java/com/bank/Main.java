package com.bank;

import javax.swing.SwingUtilities;

import com.bank.gui.MainFrame;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}