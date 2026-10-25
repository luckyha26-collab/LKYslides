package com.slidegen.model;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Incoming presentation request supporting general academic & engineering metadata
 * (Team members, project guide, department, university, topic, custom notes).
 */
public record PresentationRequest(

        @NotBlank(message = "Topic is required")
        @Size(min = 2, max = 250, message = "Topic must be between 2 and 250 characters")
        String topic,

        @Size(max = 15000, message = "Content must be at most 15000 characters")
        String content,

        @NotNull(message = "Number of slides is required")
        @Min(value = 3, message = "Minimum 3 slides")
        @Max(value = 25, message = "Maximum 25 slides")
        Integer slideCount,

        @NotBlank(message = "Theme is required")
        String theme,

        String presentedBy,
        String institution,
        String subject,

        /**
         * Academic presentation extensions
         */
        List<String> teamMembers,
        String projectGuide,
        String department,

        /**
         * Tone: academic | professional | creative | casual
         */
        String tone
) {
    public PresentationRequest(String topic, String content, Integer slideCount, String theme,
                               String presentedBy, String institution, String subject, String tone) {
        this(topic, content, slideCount, theme, presentedBy, institution, subject, null, null, null, tone);
    }

    public String effectiveContent() {
        if (content != null && !content.trim().isBlank()) {
            return content.trim();
        }
        return "Comprehensive analysis of " + topic +
                ", examining core theoretical foundations, architectural mechanisms, practical implementations, " +
                "critical operational challenges, empirical benchmarks, and future strategic trajectories.";
    }

    public String effectiveInstitution() {
        if (institution != null && !institution.isBlank()) return institution.trim();
        return "";
    }

    public String effectiveDepartment() {
        if (department != null && !department.isBlank()) return department.trim();
        if (subject != null && !subject.isBlank()) return subject.trim();
        return "";
    }

    public List<String> effectiveTeamMembers() {
        if (teamMembers != null && !teamMembers.isEmpty()) return teamMembers;
        if (presentedBy != null && !presentedBy.isBlank()) {
            return List.of(presentedBy.trim());
        }
        return List.of();
    }

    public String effectiveGuide() {
        if (projectGuide != null && !projectGuide.isBlank()) return projectGuide.trim();
        return "";
    }
}
