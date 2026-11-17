package com.skillswap.view.panels;

import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clean sub-panel displayed when selecting secondary sidebar tabs.
 */
public class PlaceholderSubPanel extends JPanel {

    public PlaceholderSubPanel(String titleText, String iconGlyph, String descriptionText) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel(iconGlyph + " " + titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(descriptionText);
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Center card with information
        CardContainer card = new CardContainer(new EmptyBorder(32, 32, 32, 32));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel glyphLabel = new JLabel(iconGlyph, SwingConstants.CENTER);
        glyphLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        glyphLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(titleText + " Section");
        descLabel.setFont(UIConstants.FONT_SUBTITLE);
        descLabel.setForeground(UIConstants.TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("This module is connected to your Firebase Realtime Database account.");
        infoLabel.setFont(UIConstants.FONT_BODY);
        infoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(glyphLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(descLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(infoLabel);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(card);

        add(centerWrap, BorderLayout.CENTER);
    }
}
