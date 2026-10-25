package com.slidegen.model;

import java.util.List;

/**
 * Outline card returned during the Gamma-style outline planning stage.
 * Users can review, reorder, edit titles, or change layout types before generation.
 */
public record OutlineCard(
        int index,
        String title,
        String layoutType,       // TITLE_SLIDE | CONTENT | TWO_COLUMN | QUOTE | STATS | TRANSITION | CONCLUSION
        String summary,
        List<String> keyPoints
) {}
