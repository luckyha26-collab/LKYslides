package com.slidegen;

import com.slidegen.engine.ContentHumanizer;
import com.slidegen.engine.PowerPointBuilder;
import com.slidegen.model.PresentationRequest;
import com.slidegen.model.SlideContent;
import com.slidegen.model.ThemeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SlideGenIntegrationTest {

    @Autowired ContentHumanizer humanizer;
    @Autowired PowerPointBuilder builder;

    private PresentationRequest buildRequest(int slides, String theme, String tone) {
        return new PresentationRequest(
            "Climate Change and Global Impact",
            "Global temperatures have risen by 1.1°C since the pre-industrial era. " +
            "The primary causes are fossil fuel combustion, deforestation, and industrial emissions. " +
            "Effects include rising sea levels, increased frequency of extreme weather events, " +
            "loss of biodiversity, and food security challenges. " +
            "Key solutions involve transitioning to renewable energy, implementing carbon pricing, " +
            "and international policy cooperation through agreements like the Paris Accord.",
            slides, theme, "Dr. Lucky", "MIT College", "Environmental Science", tone
        );
    }

    @Test
    void contextLoads() {
        assertNotNull(humanizer);
        assertNotNull(builder);
    }

    @Test
    void generatesCorrectSlideCount() {
        PresentationRequest req = buildRequest(7, "ACADEMIC_BLUE", "academic");
        List<SlideContent> slides = humanizer.buildSlides(req);
        assertFalse(slides.isEmpty(), "Should produce at least one slide");
    }

    @Test
    void generatesValidPptxBytes() throws Exception {
        PresentationRequest req = buildRequest(6, "DARK_CORPORATE", "professional");
        List<SlideContent> slides = humanizer.buildSlides(req);
        ThemeConfig theme = ThemeConfig.fromName("DARK_CORPORATE");

        byte[] bytes = builder.build(slides, theme);

        assertTrue(bytes.length > 1000, "PPTX should have substantial content (>1KB)");
        // PPTX files start with PK (ZIP signature)
        assertEquals(0x50, bytes[0] & 0xFF, "PPTX must start with PK zip signature (0x50)");
        assertEquals(0x4B, bytes[1] & 0xFF, "PPTX must start with PK zip signature (0x4B)");
    }

    @Test
    void allThemesProducePptx() throws Exception {
        String[] themes = { "DARK_CORPORATE", "MINIMAL_LIGHT", "GRADIENT_MODERN",
                            "ACADEMIC_BLUE", "EARTHY_WARM", "VIBRANT_CREATIVE" };
        for (String themeId : themes) {
            PresentationRequest req = buildRequest(5, themeId, "creative");
            List<SlideContent> slides = humanizer.buildSlides(req);
            ThemeConfig theme = ThemeConfig.fromName(themeId);
            byte[] bytes = builder.build(slides, theme);
            assertTrue(bytes.length > 500, "Theme " + themeId + " should generate a valid PPTX");
        }
    }
}
