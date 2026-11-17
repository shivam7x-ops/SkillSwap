package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable modern dark-themed metric summary card.
 */
public class SummaryCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;
    private final String iconGlyph;

    public SummaryCard(String title, String subtitle, String iconGlyph, Color accentColor) {
        this.titleLabel = new JLabel(title);
        this.subtitleLabel = new JLabel(subtitle);
        this.iconGlyph = iconGlyph;
        this.accentColor = accentColor;
        this.valueLabel = new JLabel("-");

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(18, 20, 18, 20));
        setPreferredSize(new Dimension(200, 130));
        setMinimumSize(new Dimension(170, 120));

        // Top Row: Title + Icon
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JLabel iconLabel = new JLabel(iconGlyph);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(accentColor);
        topPanel.add(iconLabel, BorderLayout.EAST);

        // Center: Metric Value
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);

        // Bottom Row: Subtitle
        subtitleLabel.setFont(UIConstants.FONT_SMALL);
        subtitleLabel.setForeground(UIConstants.TEXT_HINT);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subtitleLabel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setValue(int value) {
        valueLabel.setText(String.valueOf(value));
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);
        repaint();
    }

    public void setLoading() {
        valueLabel.setText("...");
        valueLabel.setForeground(UIConstants.TEXT_HINT);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        g2.setColor(UIConstants.BG_SECONDARY);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

        // Subtle accent bar at top
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, 4, 14, 14));

        // Card border
        g2.setColor(UIConstants.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

        g2.dispose();
        super.paintComponent(g);
    }
}
