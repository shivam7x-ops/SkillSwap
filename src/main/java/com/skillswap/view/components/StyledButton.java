package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable modern button with rounded corners, hover transitions, and dark-theme styling.
 */
public class StyledButton extends JButton {

    public enum ButtonStyle {
        PRIMARY, SECONDARY, DANGER, LINK
    }

    private final ButtonStyle buttonStyle;
    private boolean isHovered = false;
    private boolean isLoading = false;
    private String originalText;

    public StyledButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public StyledButton(String text, ButtonStyle buttonStyle) {
        super(text);
        this.buttonStyle = buttonStyle;
        this.originalText = text;

        setFont(buttonStyle == ButtonStyle.LINK ? UIConstants.FONT_BODY : UIConstants.FONT_BUTTON);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (buttonStyle != ButtonStyle.LINK) {
            setPreferredSize(new Dimension(200, UIConstants.INPUT_HEIGHT));
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        setEnabled(!loading);
        if (loading) {
            setText("Please wait...");
        } else {
            setText(originalText);
        }
        repaint();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (!isLoading) {
            this.originalText = text;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (buttonStyle == ButtonStyle.LINK) {
            // Link styling
            Color textColor = isHovered ? UIConstants.ACCENT_HOVER : UIConstants.ACCENT;
            setForeground(textColor);
            super.paintComponent(g);

            if (isHovered) {
                // Underline link on hover
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (width - textWidth) / 2;
                int y = (height + fm.getAscent()) / 2 + 2;
                g2.setColor(textColor);
                g2.drawLine(x, y, x + textWidth, y);
            }
        } else {
            // Button background
            Color bgColor;
            Color textColor;

            switch (buttonStyle) {
                case SECONDARY:
                    bgColor = isHovered ? UIConstants.BG_CARD.brighter() : UIConstants.BG_CARD;
                    textColor = UIConstants.TEXT_PRIMARY;
                    break;
                case DANGER:
                    bgColor = isHovered ? UIConstants.ACCENT_RED.brighter() : UIConstants.ACCENT_RED;
                    textColor = Color.WHITE;
                    break;
                case PRIMARY:
                default:
                    bgColor = isHovered ? UIConstants.ACCENT_HOVER : UIConstants.ACCENT;
                    textColor = new Color(20, 20, 20); // Dark text on light accent
                    break;
            }

            if (!isEnabled()) {
                bgColor = UIConstants.BG_INPUT;
                textColor = UIConstants.TEXT_HINT;
            }

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS));

            // Paint text
            setForeground(textColor);
            super.paintComponent(g);
        }

        g2.dispose();
    }
}
