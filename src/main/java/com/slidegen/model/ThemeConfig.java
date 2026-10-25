package com.slidegen.model;

import java.util.List;

/**
 * Theme configuration: Colors, fonts, and visual identity.
 */
public record ThemeConfig(
        String name,
        // Background
        int[] bgColor,         // RGB
        int[] bgAccentColor,   // secondary bg or sidebar color
        // Text colors
        int[] titleColor,
        int[] bodyColor,
        int[] accentColor,
        // Fonts
        String titleFont,
        String bodyFont,
        // Specific element colors
        int[] bulletAccentColor,
        int[] quoteColor,
        int[] statValueColor
) {
    public static ThemeConfig DARK_CORPORATE = new ThemeConfig(
            "Dark Corporate",
            new int[]{18, 24, 38},       // Navy dark bg
            new int[]{28, 38, 60},       // Slightly lighter navy
            new int[]{255, 255, 255},    // White title
            new int[]{200, 210, 230},    // Light blue-gray body
            new int[]{64, 196, 255},     // Cyan accent
            "Calibri", "Calibri",
            new int[]{64, 196, 255},
            new int[]{150, 200, 255},
            new int[]{64, 196, 255}
    );

    public static ThemeConfig MINIMAL_LIGHT = new ThemeConfig(
            "Minimal Light",
            new int[]{252, 252, 254},    // Near white bg
            new int[]{240, 240, 248},    // Very light gray
            new int[]{20, 20, 45},       // Almost black title
            new int[]{60, 60, 80},       // Dark gray body
            new int[]{80, 80, 240},      // Indigo accent
            "Segoe UI", "Segoe UI",
            new int[]{80, 80, 240},
            new int[]{80, 80, 200},
            new int[]{80, 80, 240}
    );

    public static ThemeConfig GRADIENT_MODERN = new ThemeConfig(
            "Gradient Modern",
            new int[]{15, 12, 41},       // Deep purple-black bg
            new int[]{48, 43, 99},       // Purple mid
            new int[]{255, 255, 255},    // White title
            new int[]{210, 200, 240},    // Lavender body
            new int[]{255, 100, 180},    // Pink accent
            "Gill Sans MT", "Calibri",
            new int[]{255, 100, 180},
            new int[]{200, 180, 255},
            new int[]{255, 100, 180}
    );

    public static ThemeConfig ACADEMIC_BLUE = new ThemeConfig(
            "Academic Blue",
            new int[]{10, 42, 85},       // Deep academic blue bg
            new int[]{20, 60, 115},      // Lighter navy
            new int[]{255, 255, 255},    // White title
            new int[]{200, 215, 235},    // Light body
            new int[]{255, 196, 0},      // Gold accent
            "Times New Roman", "Calibri",
            new int[]{255, 196, 0},
            new int[]{180, 200, 235},
            new int[]{255, 196, 0}
    );

    public static ThemeConfig EARTHY_WARM = new ThemeConfig(
            "Earthy Warm",
            new int[]{245, 240, 230},    // Cream bg
            new int[]{235, 225, 208},    // Warmer cream
            new int[]{80, 50, 20},       // Dark brown title
            new int[]{100, 70, 40},      // Medium brown body
            new int[]{200, 100, 40},     // Terracotta accent
            "Georgia", "Calibri",
            new int[]{200, 100, 40},
            new int[]{160, 90, 40},
            new int[]{200, 100, 40}
    );

    public static ThemeConfig VIBRANT_CREATIVE = new ThemeConfig(
            "Vibrant Creative",
            new int[]{255, 255, 255},    // White bg
            new int[]{248, 248, 255},    // Off-white
            new int[]{20, 20, 20},       // Black title
            new int[]{50, 50, 60},       // Dark body
            new int[]{255, 60, 60},      // Red accent
            "Arial Black", "Arial",
            new int[]{255, 60, 60},
            new int[]{255, 100, 60},
            new int[]{255, 60, 60}
    );

    public static ThemeConfig fromName(String name) {
        if (name == null) return DARK_CORPORATE;
        return switch (name.toUpperCase().replace(" ", "_").replace("-", "_")) {
            case "DARK_CORPORATE", "OBSIDIAN_NIGHT", "OBSIDIAN" -> DARK_CORPORATE;
            case "MINIMAL_LIGHT", "CLEAN_PAPER", "MINIMAL"      -> MINIMAL_LIGHT;
            case "GRADIENT_MODERN", "NEON_SUNSET", "GRADIENT"   -> GRADIENT_MODERN;
            case "ACADEMIC_BLUE", "ACADEMIC"                    -> ACADEMIC_BLUE;
            case "EARTHY_WARM", "EARTHY_CLAY", "EARTHY"         -> EARTHY_WARM;
            case "VIBRANT_CREATIVE", "CYBER_MINT", "CYBER"      -> VIBRANT_CREATIVE;
            default                                             -> DARK_CORPORATE;
        };
    }
}
