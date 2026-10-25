package com.slidegen.web;

import com.slidegen.engine.ContentHumanizer;
import com.slidegen.engine.PowerPointBuilder;
import com.slidegen.model.ExportDeckRequest;
import com.slidegen.model.OutlineCard;
import com.slidegen.model.PresentationRequest;
import com.slidegen.model.SlideContent;
import com.slidegen.model.ThemeConfig;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Gamma-inspired REST controller for outline preview, live interactive slide decks,
 * and high-fidelity watermark-free PowerPoint generation.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SlideGenController {

    private static final Logger log = LoggerFactory.getLogger(SlideGenController.class);
    private static final String PPTX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    private final ContentHumanizer humanizer;
    private final PowerPointBuilder builder;

    public SlideGenController(ContentHumanizer humanizer, PowerPointBuilder builder) {
        this.humanizer = humanizer;
        this.builder = builder;
    }

    /**
     * POST /api/outline
     * Gamma Phase 1: Generates an interactive outline breakdown so users can review,
     * reorder, or edit slide cards before full generation.
     */
    @PostMapping("/outline")
    public ResponseEntity<List<OutlineCard>> generateOutline(@Valid @RequestBody PresentationRequest request) {
        log.info("Generating outline | topic='{}' slides={}", request.topic(), request.slideCount());
        List<OutlineCard> outline = humanizer.buildOutline(request);
        return ResponseEntity.ok(outline);
    }

    /**
     * POST /api/generate-slides
     * Gamma Phase 2: Generates the full structured slide cards for the live in-browser
     * Gamma deck viewer and interactive presentation editor.
     */
    @PostMapping("/generate-slides")
    public ResponseEntity<List<SlideContent>> generateSlides(@Valid @RequestBody PresentationRequest request) {
        log.info("Generating slide cards | topic='{}' slides={} tone={}",
                request.topic(), request.slideCount(), request.tone());
        List<SlideContent> slides = humanizer.buildSlides(request);
        return ResponseEntity.ok(slides);
    }

    /**
     * POST /api/export-pptx
     * Gamma Phase 3: Exports the currently edited live deck cards into a native,
     * watermark-free PowerPoint (.pptx) file.
     */
    @PostMapping("/export-pptx")
    public ResponseEntity<byte[]> exportPptx(@Valid @RequestBody ExportDeckRequest request) throws IOException {
        log.info("Exporting deck to PPTX | topic='{}' slides={} theme={}",
                request.topic(), request.slides().size(), request.theme());

        ThemeConfig theme = ThemeConfig.fromName(request.theme());
        byte[] pptxBytes = builder.build(request.slides(), theme);

        String filename = sanitizeFilename(request.topic()) + "_lkyslides.pptx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .contentType(MediaType.parseMediaType(PPTX_CONTENT_TYPE))
                .contentLength(pptxBytes.length)
                .body(pptxBytes);
    }

    /**
     * POST /api/generate
     * One-click direct generation endpoint: transforms prompt straight into .pptx download.
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@Valid @RequestBody PresentationRequest request) throws IOException {
        log.info("One-click generate | topic='{}' slides={} theme={}", request.topic(), request.slideCount(), request.theme());

        List<SlideContent> slides = humanizer.buildSlides(request);
        ThemeConfig theme = ThemeConfig.fromName(request.theme());
        byte[] pptxBytes = builder.build(slides, theme);

        String filename = sanitizeFilename(request.topic()) + "_lkyslides.pptx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .contentType(MediaType.parseMediaType(PPTX_CONTENT_TYPE))
                .contentLength(pptxBytes.length)
                .body(pptxBytes);
    }

    /**
     * GET /api/themes
     * Returns Gamma-style themes with color tokens and font pairing metadata.
     */
    @GetMapping("/themes")
    public ResponseEntity<List<Map<String, Object>>> themes() {
        List<Map<String, Object>> themes = Arrays.asList(
            Map.of(
                "id",          "DARK_CORPORATE",
                "label",       "Obsidian Night",
                "description", "Sleek carbon dark with violet and electric cyan accents",
                "primary",     "#0f1117",
                "cardBg",      "#171b24",
                "accent",      "#8b5cf6",
                "accent2",     "#06b6d4",
                "text",        "#f1f5f9"
            ),
            Map.of(
                "id",          "ACADEMIC_BLUE",
                "label",       "Academic Blue",
                "description", "Prestige navy blue & warm gold — ideal for thesis & research defense",
                "primary",     "#0b1d3a",
                "cardBg",      "#112b54",
                "accent",      "#f59e0b",
                "accent2",     "#60a5fa",
                "text",        "#ffffff"
            ),
            Map.of(
                "id",          "MINIMAL_LIGHT",
                "label",       "Clean Paper",
                "description", "Ultra crisp ivory paper, graphite typography & indigo highlights",
                "primary",     "#f8fafc",
                "cardBg",      "#ffffff",
                "accent",      "#6366f1",
                "accent2",     "#0284c7",
                "text",        "#0f172a"
            ),
            Map.of(
                "id",          "GRADIENT_MODERN",
                "label",       "Neon Sunset",
                "description", "Deep twilight purple blending with warm coral and amber",
                "primary",     "#130924",
                "cardBg",      "#1f1038",
                "accent",      "#f43f5e",
                "accent2",     "#fb923c",
                "text",        "#ffffff"
            ),
            Map.of(
                "id",          "EARTHY_WARM",
                "label",       "Earthy Clay",
                "description", "Warm terracotta, sand cream, and editorial serif styling",
                "primary",     "#fcfaf7",
                "cardBg",      "#f3ebe1",
                "accent",      "#c25e2e",
                "accent2",     "#8c431b",
                "text",        "#291a10"
            ),
            Map.of(
                "id",          "VIBRANT_CREATIVE",
                "label",       "Cyber Mint",
                "description", "Pitch obsidian with vivid electric mint & high-contrast typography",
                "primary",     "#0a0f0d",
                "cardBg",      "#111c18",
                "accent",      "#10b981",
                "accent2",     "#34d399",
                "text",        "#f0fdf4"
            )
        );
        return ResponseEntity.ok(themes);
    }

    /**
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "LKYslides Presentation Engine"));
    }

    private String sanitizeFilename(String topic) {
        String safe = topic.replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "_")
                .toLowerCase();
        return safe.isEmpty() ? "presentation" : safe;
    }
}
