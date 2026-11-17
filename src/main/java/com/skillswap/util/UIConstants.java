package com.skillswap.util;

import java.awt.*;

/**
 * Centralised colour palette and dimension constants for the dark theme.
 *
 * <p>Every UI class should reference these constants instead of creating
 * ad-hoc colours, so the theme can be changed in one place.
 */
public final class UIConstants {

    private UIConstants() { /* utility class */ }

    // ── Colours ────────────────────────────────────────────────────────
    public static final Color BG_PRIMARY      = new Color(30, 30, 30);
    public static final Color BG_SECONDARY    = new Color(45, 45, 45);
    public static final Color BG_CARD         = new Color(55, 55, 55);
    public static final Color BG_INPUT        = new Color(60, 60, 60);

    public static final Color ACCENT          = new Color(100, 180, 255);   // soft blue
    public static final Color ACCENT_HOVER    = new Color(130, 200, 255);
    public static final Color ACCENT_GREEN    = new Color(80, 200, 120);
    public static final Color ACCENT_RED      = new Color(230, 80, 80);
    public static final Color ACCENT_YELLOW   = new Color(255, 200, 60);

    public static final Color TEXT_PRIMARY    = new Color(230, 230, 230);
    public static final Color TEXT_SECONDARY  = new Color(160, 160, 160);
    public static final Color TEXT_HINT       = new Color(120, 120, 120);

    public static final Color BORDER          = new Color(70, 70, 70);

    // ── Fonts ──────────────────────────────────────────────────────────
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON      = new Font("Segoe UI", Font.BOLD,  14);

    // ── Dimensions ─────────────────────────────────────────────────────
    public static final int PADDING           = 16;
    public static final int PADDING_SMALL     = 8;
    public static final int BORDER_RADIUS     = 12;
    public static final int INPUT_HEIGHT      = 40;

    public static final Dimension FRAME_SIZE  = new Dimension(1100, 750);
}
