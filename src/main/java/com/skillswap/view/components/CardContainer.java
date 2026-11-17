package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable dark card container with rounded corners and subtle border.
 */
public class CardContainer extends JPanel {

    public CardContainer() {
        this(new EmptyBorder(30, 40, 30, 40));
    }

    public CardContainer(EmptyBorder padding) {
        setOpaque(false);
        setBorder(padding);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Card background
        g2.setColor(UIConstants.BG_SECONDARY);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 16, 16));

        // Card border
        g2.setColor(UIConstants.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, 16, 16));

        g2.dispose();
        super.paintComponent(g);
    }
}
