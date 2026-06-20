package com.bank.gui.style;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

public final class UIStyle {

    // Shared colors
    public static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    public static final Color TITLE_COLOR =
            new Color(45, 55, 72);

    public static final Color BUTTON_COLOR =
            new Color(66, 133, 244);

    public static final Color BUTTON_TEXT_COLOR =
            Color.WHITE;

    // Shared fonts
    public static final Font NORMAL_FONT =
            new Font("Segoe UI", Font.PLAIN, 14);

    public static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 20);

    public static final Font TABLE_HEADER_FONT =
            new Font("Segoe UI", Font.BOLD, 14);

    // Prevent creating UIStyle objects.
    private UIStyle() {
    }

    //Applies the common background, font, and padding to a panel.
     
    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setFont(NORMAL_FONT);
    }

    // Styles labels used as screen or section titles.
    public static void styleTitle(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(TITLE_COLOR);
    }

    //Applies the same appearance to application buttons.

    public static void styleButton(JButton button) {
        button.setFont(NORMAL_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Styles the table rows and table header.
    public static void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 232, 252));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(230, 234, 240));
        header.setForeground(TITLE_COLOR);
        header.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0,
                        new Color(200, 205, 215)
                )
        );
    }
}