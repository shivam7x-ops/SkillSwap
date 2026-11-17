package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern styled navigation button for the left sidebar.
 */
public class SidebarButton extends JButton {

    private final String iconGlyph;
    private final boolean isDanger;
    private boolean isActive = false;
    private boolean isHovered = false;

    public SidebarButton(String text, String iconGlyph) {
        this(text, iconGlyph, false);
    }

    public SidebarButton(String text, String iconGlyph, boolean isDanger) {
        super(text);
        this.iconGlyph = iconGlyph;
        this.isDanger = isDanger;

        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setForeground(isDanger ? UIConstants.ACCENT_RED : UIConstants.TEXT_SECONDARY);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

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

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(UIConstants.ACCENT);
        } else {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(isDanger ? UIConstants.ACCENT_RED : UIConstants.TEXT_SECONDARY);
        }
        repaint();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background highlight
        if (isActive) {
            // Selected background
            g2.setColor(new Color(100, 180, 255, 35));
            g2.fill(new RoundRectangle2D.Float(8, 2, w - 16, h - 4, 10, 10));

            // Left accent indicator strip
            g2.setColor(UIConstants.ACCENT);
            g2.fill(new RoundRectangle2D.Float(8, 6, 4, h - 12, 3, 3));
        } else if (isHovered) {
            Color hoverBg = isDanger ? new Color(230, 80, 80, 25) : new Color(255, 255, 255, 15);
            g2.setColor(hoverBg);
            g2.fill(new RoundRectangle2D.Float(8, 2, w - 16, h - 4, 10, 10));
        }

        // Draw Icon and Text manually for pixel-perfect alignment
        FontMetrics fm = g2.getFontMetrics();
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();

        // Icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        Color iconColor = isDanger ? UIConstants.ACCENT_RED : (isActive ? UIConstants.ACCENT : (isHovered ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_SECONDARY));
        g2.setColor(iconColor);
        g2.drawString(iconGlyph, 24, y);

        // Text
        g2.setFont(getFont());
        Color textColor = isDanger ? (isHovered ? UIConstants.ACCENT_RED.brighter() : UIConstants.ACCENT_RED)
                : (isActive ? UIConstants.ACCENT : (isHovered ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_SECONDARY));
        g2.setColor(textColor);
        g2.drawString(getText(), 52, y);

        g2.dispose();
    }
}
