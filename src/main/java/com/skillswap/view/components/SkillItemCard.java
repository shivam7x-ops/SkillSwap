package com.skillswap.view.components;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Modern interactive skill card/tag with Edit and Delete actions.
 */
public class SkillItemCard extends JPanel {

    public enum Type {
        TEACH, LEARN
    }

    private final String skillName;
    private final Type type;
    private final Consumer<String> onEdit;
    private final Consumer<String> onDelete;

    private boolean isHovered = false;

    public SkillItemCard(String skillName, Type type, Consumer<String> onEdit, Consumer<String> onDelete) {
        this.skillName = skillName;
        this.type = type;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setLayout(new BorderLayout(8, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(6, 12, 6, 8));

        Color themeColor = (type == Type.TEACH) ? UIConstants.ACCENT : UIConstants.ACCENT_GREEN;

        // Skill Name Label
        JLabel nameLabel = new JLabel(skillName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(themeColor);
        add(nameLabel, BorderLayout.CENTER);

        // Action Buttons Row (Edit + Delete)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionsPanel.setOpaque(false);

        // Edit Button (✏️)
        JButton btnEdit = createActionButton("✏️", "Edit skill", themeColor);
        btnEdit.addActionListener(e -> {
            if (onEdit != null) {
                onEdit.accept(skillName);
            }
        });

        // Delete Button (✕)
        JButton btnDelete = createActionButton("✕", "Delete skill", UIConstants.ACCENT_RED);
        btnDelete.addActionListener(e -> {
            if (onDelete != null) {
                onDelete.accept(skillName);
            }
        });

        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);
        add(actionsPanel, BorderLayout.EAST);
    }

    private JButton createActionButton(String text, String tooltip, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        btn.setForeground(UIConstants.TEXT_HINT);
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(2, 4, 2, 4));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(UIConstants.TEXT_HINT);
            }
        });

        return btn;
    }

    public String getSkillName() {
        return skillName;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        Color bg = (type == Type.TEACH)
                ? new Color(100, 180, 255, 25)
                : new Color(80, 200, 120, 25);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

        // Border
        Color border = (type == Type.TEACH)
                ? new Color(100, 180, 255, 80)
                : new Color(80, 200, 120, 80);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

        g2.dispose();
        super.paintComponent(g);
    }
}
