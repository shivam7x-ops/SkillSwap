package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable dark-themed modern text field with rounded borders, focus glow, and placeholder text.
 */
public class StyledTextField extends JTextField {

    private String placeholder;
    private boolean isFocused = false;

    public StyledTextField(String placeholder) {
        this.placeholder = placeholder;

        setFont(UIConstants.FONT_BODY);
        setForeground(UIConstants.TEXT_PRIMARY);
        setCaretColor(UIConstants.ACCENT);
        setBackground(UIConstants.BG_INPUT);
        setOpaque(false);
        setBorder(new EmptyBorder(8, 14, 8, 14));
        setPreferredSize(new Dimension(300, UIConstants.INPUT_HEIGHT));

        addFocusListener(new FocusAdapter() {
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

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
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

        // Border: highlight on focus
        Color borderColor = isFocused ? UIConstants.ACCENT : UIConstants.BORDER;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(isFocused ? 1.8f : 1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS));

        // Standard text
        super.paintComponent(g);

        // Placeholder text
        if (getText().isEmpty() && placeholder != null && !isFocused) {
            g2.setColor(UIConstants.TEXT_HINT);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            Insets insets = getInsets();
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, insets.left, y);
        }

        g2.dispose();
    }
}
