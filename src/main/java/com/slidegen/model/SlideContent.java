package com.slidegen.model;

import java.util.List;

/**
 * SlideContent: Structured representation of an academic, engineering, or professional presentation slide.
 * Supports Title, Agenda, Abstract, Problem Statement, Objectives, Scope (In/Out),
 * Methodology, Technology Stack, Expected Outcomes, Future Scope, References, and Thank You.
 */
public record SlideContent(
        String layoutType,           // TITLE_SLIDE | AGENDA | ABSTRACT | BULLETS | SCOPE | METHODOLOGY | TABLE | TWO_COLUMN | GRID_CARDS | ALGORITHM_FLOW | METRICS_DETAIL | REFERENCES | THANK_YOU
        String title,
        String subtitle,
        List<String> bullets,
        String leftColumn,           // Left column text / In Scope list
        String rightColumn,          // Right column text / Out of Scope list
        String quote,
        String quoteAuthor,
        List<String> statLabels,
        List<String> statValues,
        String speakerNote,
        int slideNumber,

        // ── Structured Layout Fields ─────────────────────────────────────────
        String runningHeader,        // e.g. "Project Topic | University / Department"
        String calloutMetric,        // Single metric (optional)
        String calloutLabel,         // Metric label (optional)
        String calloutDescription,   // Abstract text / Description
        List<String> cardTitles,     // Methodology phase titles / Agenda items
        List<String> cardDescriptions, // Methodology phase descriptions
        List<String> tableHeaders,   // Tech stack / Comparison table headers
        List<List<String>> tableRows,// Tech stack / Comparison table rows
        String calloutNote,          // Bottom note / highlight
        List<String> algorithmInputs,// Inputs
        String algorithmCoreName,    // Engine / Core name
        String algorithmCoreDesc,    // Engine / Core description
        List<String> algorithmTiers, // Output tiers / stages
        List<String> teamMembers,    // Project team / authors
        String projectGuide          // Project guide / supervisor
) {

    // ── Standard Academic & Project Layout Constructors ───────────────────────

    public static SlideContent titleSlide(String title, String subtitle, List<String> teamMembers,
                                          String guide, String institution, String department, int num) {
        String running = title + (institution != null && !institution.isBlank() ? " | " + institution : "");
        String note = "Introduce yourself, the presentation scope, and the project team / supervisor.";
        return new SlideContent(
                "TITLE_SLIDE", title, subtitle, null, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null, null, null,
                null, null, null, teamMembers, guide
        );
    }

    public static SlideContent agendaSlide(String title, List<String> items, String running, int num) {
        String note = "Walk evaluators through the presentation roadmap from Abstract to References.";
        return new SlideContent(
                "AGENDA", title != null ? title : "Content", null, items, null, null, null, null, null, null,
                note, num, running, null, null, null, items, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent abstractSlide(String title, String abstractText, String running, int num) {
        String note = "Deliver a concise high-level summary of the motivation, core challenge, and proposed solution.";
        return new SlideContent(
                "ABSTRACT", title != null ? title : "Abstract", null, null, null, null, null, null, null, null,
                note, num, running, null, null, abstractText, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent bulletsSlide(String title, List<String> bullets, String running, String note, int num) {
        return new SlideContent(
                "BULLETS", title, null, bullets, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent scopeSlide(String title, List<String> inScope, List<String> outOfScope, String running, int num) {
        String note = "Define project boundaries: explicitly distinguish what is built vs what lies outside project scope.";
        String left = inScope != null ? String.join("\n• ", inScope) : "";
        if (!left.isBlank()) left = "• " + left;
        String right = outOfScope != null ? String.join("\n• ", outOfScope) : "";
        if (!right.isBlank()) right = "• " + right;

        return new SlideContent(
                "SCOPE", title != null ? title : "Scope", null, inScope, left, right, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent methodologySlide(String title, List<String> phaseTitles, List<String> phaseDescs, String running, int num) {
        String note = "Explain the step-by-step engineering/research methodology from requirement analysis to final evaluation.";
        return new SlideContent(
                "METHODOLOGY", title != null ? title : "Proposed Methodology", null, null, null, null, null, null, null, null,
                note, num, running, null, null, null, phaseTitles, phaseDescs, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent tableSlide(String title, List<String> headers, List<List<String>> rows,
                                          String highlightNote, String running, String note, int num) {
        return new SlideContent(
                "TABLE", title, null, null, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, headers, rows,
                highlightNote, null, null, null, null, null, null
        );
    }

    public static SlideContent twoColumnSlide(String title, String left, String right, String running, String note, int num) {
        return new SlideContent(
                "TWO_COLUMN", title, null, null, left, right, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent gridCards(String title, List<String> titles, List<String> descriptions,
                                         String running, String note, int num) {
        return new SlideContent(
                "GRID_CARDS", title, null, null, null, null, null, null, null, null,
                note, num, running, null, null, null, titles, descriptions, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent splitCallout(String title, List<String> bullets, String metric,
                                           String metricLabel, String metricDesc, String running,
                                           String note, int num) {
        return new SlideContent(
                "SPLIT_CALLOUT", title, null, bullets, null, null, null, null, null, null,
                note, num, running, metric, metricLabel, metricDesc, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent algorithmFlow(String title, List<String> inputs, String coreName,
                                            String coreDesc, List<String> tiers, String note,
                                            String formulaNote, String running, int num) {
        return new SlideContent(
                "ALGORITHM_FLOW", title, null, null, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                formulaNote, inputs, coreName, coreDesc, tiers, null, null
        );
    }

    public static SlideContent metricsDetail(String title, List<String> labels, List<String> values,
                                             List<String> bullets, String running, String note, int num) {
        return new SlideContent(
                "METRICS_DETAIL", title, null, bullets, null, null, null, null, labels, values,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent contentSlide(String title, List<String> bullets, String running, String note, int num) {
        return new SlideContent(
                "CONTENT", title, null, bullets, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent referencesSlide(String title, List<String> references, String running, String note, int num) {
        return new SlideContent(
                "REFERENCES", title, null, references, null, null, null, null, null, null,
                note, num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    public static SlideContent thankYouSlide(String title, String subtitle, String running, int num) {
        return new SlideContent(
                "THANK_YOU", title != null ? title : "Thank You !!", subtitle, null, null, null, null, null, null, null,
                "Acknowledge the audience and evaluators. Open the floor for questions and discussion.",
                num, running, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
