package com.skillswap;

import com.formdev.flatlaf.FlatDarkLaf;
import com.skillswap.config.FirebaseConfig;
import com.skillswap.view.MainFrame;

import javax.swing.*;

/**
 * Application entry point for SkillSwap – Student Skill Exchange Platform.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Install the FlatLaf dark look-and-feel.</li>
 *   <li>Initialise Firebase (Auth + Realtime Database).</li>
 *   <li>Launch the main Swing window on the EDT.</li>
 * </ol>
 */
public class SkillSwapApp {

    public static void main(String[] args) {
        // 1. Set the modern dark theme before any Swing component is created
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to set FlatLaf dark theme – falling back to default.");
        }

        // 2. Initialise Firebase services
        try {
            FirebaseConfig.initialize();
            System.out.println("Firebase initialised successfully.");
        } catch (Exception e) {
            System.err.println("Firebase initialisation failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Could not connect to Firebase.\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
