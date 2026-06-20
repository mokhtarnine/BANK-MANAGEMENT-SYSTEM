package com.bank.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bank.gui.style.UIStyle;

public class AlertLogPanel extends JPanel {

    private final JTextArea alertTextArea;


    public AlertLogPanel() {
        this.alertTextArea = new JTextArea();

        buildLayout();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        UIStyle.stylePanel(this);

        alertTextArea.setEditable(false);
        alertTextArea.setFont(UIStyle.NORMAL_FONT);
        alertTextArea.setBackground(Color.WHITE);
        alertTextArea.setLineWrap(true);
        alertTextArea.setWrapStyleWord(true);

        add(
                new JScrollPane(alertTextArea),
                BorderLayout.CENTER
        );

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton testButton = new JButton("Test Alert");
        UIStyle.styleButton(testButton);
        testButton.addActionListener(e -> addAlert(
                "Test alert message"
        ));

        JButton clearButton = new JButton("Clear");
        UIStyle.styleButton(clearButton);
        clearButton.addActionListener(e -> clearAlerts());

        buttonPanel.add(testButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addAlert(String message) {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        alertTextArea.append(
                "[" + timestamp + "] " + message + "\n"
        );
    }

    public void clearAlerts() {
        alertTextArea.setText("");
    }

    
}