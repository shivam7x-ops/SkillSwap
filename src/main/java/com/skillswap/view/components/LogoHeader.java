package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable header with SkillSwap branding, stylized logo badge, and tagline.
 */
public class LogoHeader extends JPanel {

    public LogoHeader(String subtitleText) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stylized Brand Badge Panel
        JPanel badgePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int size = 52;
                int x = (w - size) / 2;
                int y = (h - size) / 2;

                // Gradient background badge
                GradientPaint gp = new GradientPaint(x, y, UIConstants.ACCENT, x + size, y + size, new Color(50, 120, 220));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(x, y, size, size, 14, 14));

                // Exchange Arrows Icon inside badge
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "⇄";
                int tx = x + (size - fm.stringWidth(icon)) / 2;
                int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, tx, ty);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 60);
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App Title
        JLabel titleLabel = new JLabel("SkillSwap");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline / Subtitle
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(UIConstants.FONT_BODY);
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(badgePanel);
        add(Box.createVerticalStrut(4));
        add(titleLabel);
        add(Box.createVerticalStrut(4));
        add(subtitleLabel);
        add(Box.createVerticalStrut(20));
    }
}
