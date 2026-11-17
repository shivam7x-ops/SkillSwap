package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable dark-themed password field with built-in Show/Hide password toggle.
 */
public class StyledPasswordField extends JPanel {

    private final JPasswordField passwordField;
    private final JButton toggleButton;
    private String placeholder;
    private boolean isPasswordVisible = false;
    private boolean isFocused = false;
    private static final char DEFAULT_ECHO_CHAR = '\u2022'; // bullet

    public StyledPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(300, UIConstants.INPUT_HEIGHT));

        passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw placeholder if empty and not focused
                if (getPassword().length == 0 && placeholder != null && !isFocused) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UIConstants.TEXT_HINT);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    Insets insets = getInsets();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, insets.left, y);
                    g2.dispose();
                }
            }
        };

        passwordField.setFont(UIConstants.FONT_BODY);
        passwordField.setForeground(UIConstants.TEXT_PRIMARY);
        passwordField.setCaretColor(UIConstants.ACCENT);
        passwordField.setOpaque(false);
        passwordField.setBorder(new EmptyBorder(8, 14, 8, 4));
        passwordField.setEchoChar(DEFAULT_ECHO_CHAR);

        toggleButton = new JButton("👁");
        toggleButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        toggleButton.setForeground(UIConstants.TEXT_SECONDARY);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setOpaque(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Show password");
        toggleButton.setBorder(new EmptyBorder(0, 4, 0, 10));

        toggleButton.addActionListener(e -> togglePasswordVisibility());

        add(passwordField, BorderLayout.CENTER);
        add(toggleButton, BorderLayout.EAST);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setEchoChar((char) 0); // Show plain text
            toggleButton.setText("🔒");
            toggleButton.setToolTipText("Hide password");
        } else {
            passwordField.setEchoChar(DEFAULT_ECHO_CHAR); // Mask
            toggleButton.setText("👁");
            toggleButton.setToolTipText("Show password");
        }
        passwordField.requestFocusInWindow();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public String getPasswordString() {
        return new String(passwordField.getPassword());
    }

    public void setText(String text) {
        passwordField.setText(text);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(UIConstants.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS));

        // Border
        Color borderColor = isFocused ? UIConstants.ACCENT : UIConstants.BORDER;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(isFocused ? 1.8f : 1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS));

        g2.dispose();
        super.paintComponent(g);
    }
}
