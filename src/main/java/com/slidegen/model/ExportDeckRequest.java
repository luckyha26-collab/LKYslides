package com.slidegen.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request payload when exporting custom edited cards from the Gamma Deck Studio to PPTX.
 */
public record ExportDeckRequest(
        @NotBlank(message = "Topic is required")
        String topic,

        @NotBlank(message = "Theme is required")
        String theme,

        @NotEmpty(message = "Deck must contain at least one slide")
        List<SlideContent> slides
) {}
