package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable rounded chip / tag component for displaying skills.
 */
public class SkillChip extends JLabel {

    public enum ChipType {
        TEACH, LEARN
    }

    private final ChipType chipType;

    public SkillChip(String skillName, ChipType chipType) {
        super(skillName);
        this.chipType = chipType;

        setFont(UIConstants.FONT_SMALL);
        setOpaque(false);
        setBorder(new EmptyBorder(6, 12, 6, 12));

        if (chipType == ChipType.TEACH) {
            setForeground(UIConstants.ACCENT);
        } else {
            setForeground(UIConstants.ACCENT_GREEN);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background pill
        Color bg = chipType == ChipType.TEACH
                ? new Color(100, 180, 255, 30)
                : new Color(80, 200, 120, 30);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

        // Pill border
        Color border = chipType == ChipType.TEACH
                ? new Color(100, 180, 255, 100)
                : new Color(80, 200, 120, 100);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

        g2.dispose();
        super.paintComponent(g);
    }
}
