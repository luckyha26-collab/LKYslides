package com.slidegen.engine;

import com.slidegen.model.SlideContent;
import com.slidegen.model.ThemeConfig;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * PowerPointBuilder: Universal high-fidelity Apache POI generator for college & professional decks.
 * Calibrated precisely to the universal 16:9 Widescreen standard:
 * 13.333" x 7.5" = 960 pt x 540 pt (12,192,000 x 6,858,000 EMUs).
 * Opens flawlessly in Microsoft PowerPoint, Google Slides, Keynote, and LibreOffice with ZERO watermarks.
 */
@Service
public class PowerPointBuilder {

    // Universal 16:9 widescreen presentation dimensions in points (1 pt = 1/72 inch)
    // 960 pt = 13.333 inches; 540 pt = 7.5 inches.
    private static final int SLIDE_WIDTH = 960;
    private static final int SLIDE_HEIGHT = 540;

    public byte[] build(List<SlideContent> slides, ThemeConfig theme) throws IOException {
        try (XMLSlideShow pptx = new XMLSlideShow();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            pptx.setPageSize(new Dimension(SLIDE_WIDTH, SLIDE_HEIGHT));

            for (SlideContent slide : slides) {
                XSLFSlide xslide = pptx.createSlide();
                clearSlideBackground(xslide, theme);

                switch (slide.layoutType()) {
                    case "TITLE_SLIDE"    -> renderTitleSlide(xslide, slide, theme);
                    case "AGENDA"         -> renderAgendaSlide(xslide, slide, theme);
                    case "ABSTRACT"       -> renderAbstractSlide(xslide, slide, theme);
                    case "SCOPE"          -> renderScopeSlide(xslide, slide, theme);
                    case "METHODOLOGY"    -> renderMethodologySlide(xslide, slide, theme);
                    case "BULLETS"        -> renderContentSlide(xslide, slide, theme);
                    case "SPLIT_CALLOUT"  -> renderSplitCalloutSlide(xslide, slide, theme);
                    case "GRID_CARDS"     -> renderGridCardsSlide(xslide, slide, theme);
                    case "TABLE"          -> renderTableSlide(xslide, slide, theme);
                    case "ALGORITHM_FLOW" -> renderAlgorithmFlowSlide(xslide, slide, theme);
                    case "METRICS_DETAIL" -> renderMetricsDetailSlide(xslide, slide, theme);
                    case "TWO_COLUMN"     -> renderTwoColumnSlide(xslide, slide, theme);
                    case "QUOTE"          -> renderQuoteSlide(xslide, slide, theme);
                    case "REFERENCES"     -> renderReferencesSlide(xslide, slide, theme);
                    case "THANK_YOU"      -> renderThankYouSlide(xslide, slide, theme);
                    default               -> renderContentSlide(xslide, slide, theme);
                }

                // Header & Footer on all slides except Thank You
                if (!"THANK_YOU".equals(slide.layoutType())) {
                    addRunningFooter(xslide, slide.runningHeader(), slide.slideNumber(), theme);
                }

                addSpeakerNote(xslide, slide.speakerNote());
            }

            pptx.write(out);
            return out.toByteArray();
        }
    }

    // ── 1. TITLE SLIDE ────────────────────────────────────────────────────────
    private void renderTitleSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        String inst = (sc.runningHeader() != null && sc.runningHeader().contains("|"))
                ? sc.runningHeader().substring(sc.runningHeader().indexOf("|") + 1).trim()
                : "";

        // Institutional header (Top)
        XSLFTextBox instBox = slide.createTextBox();
        instBox.setAnchor(new Rectangle(45, 20, 870, 60));
        XSLFTextParagraph ip = instBox.getTextParagraphs().get(0);
        ip.setTextAlign(TextParagraph.TextAlign.CENTER);

        if (!inst.isBlank()) {
            XSLFTextRun ir1 = ip.addNewTextRun();
            ir1.setText(inst.toUpperCase() + "\n");
            ir1.setFontSize(14.0);
            ir1.setBold(true);
            ir1.setFontColor(rgb(t.accentColor()));
            ir1.setFontFamily(t.titleFont());

            XSLFTextRun ir2 = ip.addNewTextRun();
            ir2.setText("Academic & Professional Presentation");
            ir2.setFontSize(10.5);
            ir2.setFontColor(new Color(180, 190, 205));
            ir2.setFontFamily(t.bodyFont());
        }

        // Dark Central Banner Card
        XSLFAutoShape banner = slide.createAutoShape();
        banner.setShapeType(ShapeType.ROUND_RECT);
        banner.setAnchor(new Rectangle(45, 90, 870, 210));
        setFill(banner, t.bgAccentColor());
        banner.setLineColor(rgb(t.accentColor()));

        // Banner Title
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(70, 105, 820, 180));
        XSLFTextParagraph tp = titleBox.getTextParagraphs().get(0);
        XSLFTextRun tr = tp.addNewTextRun();
        tr.setText(sc.title());
        tr.setFontSize(28.0);
        tr.setBold(true);
        tr.setFontColor(rgb(t.titleColor()));
        tr.setFontFamily(t.titleFont());
        tp.setSpaceAfter(8.0);

        // Subtitle
        if (sc.subtitle() != null && !sc.subtitle().isBlank()) {
            XSLFTextParagraph subP = titleBox.addNewTextParagraph();
            XSLFTextRun sr = subP.addNewTextRun();
            sr.setText(sc.subtitle());
            sr.setFontSize(15.0);
            sr.setFontColor(rgb(t.accentColor()));
            sr.setFontFamily(t.bodyFont());
        }

        // Presenters / Authors (Left - only rendered if provided)
        if (sc.teamMembers() != null && !sc.teamMembers().isEmpty()) {
            XSLFTextBox teamBox = slide.createTextBox();
            teamBox.setAnchor(new Rectangle(65, 325, 400, 150));
            XSLFTextParagraph hp = teamBox.getTextParagraphs().get(0);
            XSLFTextRun hr = hp.addNewTextRun();
            hr.setText("Authors / Presenters\n");
            hr.setFontSize(13.0);
            hr.setBold(true);
            hr.setFontColor(rgb(t.accentColor()));
            hr.setFontFamily(t.titleFont());

            for (String m : sc.teamMembers()) {
                XSLFTextParagraph mp = teamBox.addNewTextParagraph();
                XSLFTextRun mr = mp.addNewTextRun();
                mr.setText(m);
                mr.setFontSize(11.0);
                mr.setFontColor(rgb(t.bodyColor()));
                mr.setFontFamily(t.bodyFont());
            }
        }

        // Project Guide (Right - only rendered if provided)
        if (sc.projectGuide() != null && !sc.projectGuide().isBlank()) {
            XSLFTextBox guideBox = slide.createTextBox();
            guideBox.setAnchor(new Rectangle(515, 325, 380, 150));
            XSLFTextParagraph gp = guideBox.getTextParagraphs().get(0);
            XSLFTextRun gr = gp.addNewTextRun();
            gr.setText("Faculty Advisor / Guide\n");
            gr.setFontSize(13.0);
            gr.setBold(true);
            gr.setFontColor(rgb(t.accentColor()));
            gr.setFontFamily(t.titleFont());

            XSLFTextParagraph gmp = guideBox.addNewTextParagraph();
            XSLFTextRun gmr = gmp.addNewTextRun();
            gmr.setText(sc.projectGuide());
            gmr.setFontSize(11.5);
            gmr.setFontColor(rgb(t.bodyColor()));
            gmr.setFontFamily(t.bodyFont());
        }
    }

    // ── 2. SPLIT CALLOUT SLIDE ───────────────────────────────────────────────
    private void renderSplitCalloutSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        // Left bullets area
        XSLFTextBox leftBox = slide.createTextBox();
        leftBox.setAnchor(new Rectangle(45, 95, 550, 390));
        if (sc.bullets() != null) {
            boolean first = true;
            for (String b : sc.bullets()) {
                XSLFTextParagraph p = first ? leftBox.getTextParagraphs().get(0) : leftBox.addNewTextParagraph();
                first = false;
                p.setSpaceAfter(12.0);
                p.setIndent(8.0);

                XSLFTextRun dot = p.addNewTextRun();
                dot.setText("•  ");
                dot.setFontColor(rgb(t.accentColor()));
                dot.setFontSize(13.0);
                dot.setBold(true);

                XSLFTextRun tr = p.addNewTextRun();
                tr.setText(b);
                tr.setFontColor(rgb(t.bodyColor()));
                tr.setFontSize(12.0);
                tr.setFontFamily(t.bodyFont());
            }
        }

        // Right Dark Callout Box
        XSLFAutoShape card = slide.createAutoShape();
        card.setShapeType(ShapeType.ROUND_RECT);
        card.setAnchor(new Rectangle(620, 95, 295, 390));
        setFill(card, t.bgAccentColor());
        card.setLineColor(rgb(t.accentColor()));

        XSLFTextBox cardText = slide.createTextBox();
        cardText.setAnchor(new Rectangle(635, 115, 265, 350));

        // Large Metric
        XSLFTextParagraph mp = cardText.getTextParagraphs().get(0);
        mp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun mr = mp.addNewTextRun();
        mr.setText((sc.calloutMetric() != null ? sc.calloutMetric() : "99.4%") + "\n");
        mr.setFontSize(28.0);
        mr.setBold(true);
        mr.setFontColor(rgb(t.accentColor()));
        mr.setFontFamily(t.titleFont());

        // Label
        XSLFTextParagraph lp = cardText.addNewTextParagraph();
        lp.setTextAlign(TextParagraph.TextAlign.CENTER);
        lp.setSpaceAfter(14.0);
        XSLFTextRun lr = lp.addNewTextRun();
        lr.setText((sc.calloutLabel() != null ? sc.calloutLabel() : "Benchmark Verification Metric") + "\n");
        lr.setFontSize(13.0);
        lr.setFontColor(new Color(220, 220, 230));
        lr.setFontFamily(t.bodyFont());

        // Explanation body
        XSLFTextParagraph bp = cardText.addNewTextParagraph();
        bp.setTextAlign(TextParagraph.TextAlign.LEFT);
        XSLFTextRun br = bp.addNewTextRun();
        br.setText(sc.calloutDescription() != null
                ? sc.calloutDescription()
                : "Observed performance metric under experimental evaluation conditions. Demonstrates measurable optimization over baseline legacy approaches.");
        br.setFontSize(11.5);
        br.setFontColor(new Color(190, 200, 220));
        br.setFontFamily(t.bodyFont());
    }

    // ── 3. GRID CARDS SLIDE ──────────────────────────────────────────────────
    private void renderGridCardsSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        List<String> titles = sc.cardTitles() != null ? sc.cardTitles() : List.of("Objective 1", "Objective 2", "Objective 3", "Objective 4");
        List<String> descs = sc.cardDescriptions() != null ? sc.cardDescriptions() : List.of("Details 1", "Details 2", "Details 3", "Details 4");

        int count = Math.min(8, titles.size());
        int cols = 2;
        int rows = (int) Math.ceil((double) count / cols);

        int startX = 45, startY = 95;
        int cardW = 425;
        int gapX = 20;

        int cardH;
        int gapY;
        if (rows <= 2) {
            cardH = 175;
            gapY = 16;
        } else if (rows == 3) {
            cardH = 112;
            gapY = 12;
        } else {
            cardH = 82;
            gapY = 10;
        }

        for (int i = 0; i < count; i++) {
            int r = i / cols;
            int c = i % cols;
            int x = startX + c * (cardW + gapX);
            int y = startY + r * (cardH + gapY);

            // Card background shape
            XSLFAutoShape shape = slide.createAutoShape();
            shape.setShapeType(ShapeType.ROUND_RECT);
            shape.setAnchor(new Rectangle(x, y, cardW, cardH));
            setFill(shape, t.bgAccentColor());
            shape.setLineColor(new Color(255, 255, 255, 30));

            // Text inside card
            XSLFTextBox box = slide.createTextBox();
            box.setAnchor(new Rectangle(x + 14, y + 10, cardW - 28, cardH - 20));

            // Title
            XSLFTextParagraph tp = box.getTextParagraphs().get(0);
            XSLFTextRun tr = tp.addNewTextRun();
            tr.setText(titles.get(i) + "\n");
            tr.setFontSize(12.5);
            tr.setBold(true);
            tr.setFontColor(rgb(t.accentColor()));
            tr.setFontFamily(t.titleFont());

            // Description
            if (i < descs.size()) {
                XSLFTextParagraph dp = box.addNewTextParagraph();
                XSLFTextRun dr = dp.addNewTextRun();
                dr.setText(descs.get(i));
                dr.setFontSize(10.5);
                dr.setFontColor(rgb(t.bodyColor()));
                dr.setFontFamily(t.bodyFont());
            }
        }
    }

    // ── 4. TABLE SLIDE ───────────────────────────────────────────────────────
    private void renderTableSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        List<String> headers = sc.tableHeaders() != null ? sc.tableHeaders() : List.of("Col 1", "Col 2", "Col 3");
        List<List<String>> rows = sc.tableRows() != null ? sc.tableRows() : List.of();

        int numCols = headers.size();
        int numRows = rows.size() + 1; // +1 for header

        int tableX = 45, tableY = 95, tableW = 870;
        int hasGapNote = (sc.calloutNote() != null && !sc.calloutNote().isBlank()) ? 55 : 0;
        int tableH = 390 - hasGapNote;

        XSLFTable table = slide.createTable(numRows, numCols);
        table.setAnchor(new Rectangle(tableX, tableY, tableW, tableH));

        // Format column widths
        double colWidth = (double) tableW / numCols;
        for (int c = 0; c < numCols; c++) {
            table.setColumnWidth(c, colWidth);
        }

        // Header Row
        XSLFTableRow headerRow = table.getRows().get(0);
        headerRow.setHeight(28);
        for (int c = 0; c < numCols; c++) {
            XSLFTableCell cell = headerRow.getCells().get(c);
            cell.setFillColor(rgb(t.bgAccentColor()));
            cell.setBorderColor(org.apache.poi.sl.usermodel.TableCell.BorderEdge.bottom, rgb(t.accentColor()));
            cell.setBorderWidth(org.apache.poi.sl.usermodel.TableCell.BorderEdge.bottom, 1.5);

            XSLFTextParagraph p = cell.getTextParagraphs().isEmpty() ? cell.addNewTextParagraph() : cell.getTextParagraphs().get(0);
            XSLFTextRun r = p.addNewTextRun();
            r.setText(headers.get(c));
            r.setBold(true);
            r.setFontSize(11.5);
            r.setFontColor(rgb(t.accentColor()));
            r.setFontFamily(t.titleFont());
        }

        // Data Rows
        for (int r = 0; r < rows.size(); r++) {
            XSLFTableRow dataRow = table.getRows().get(r + 1);
            dataRow.setHeight(22);
            List<String> rowData = rows.get(r);

            Color rowBg = (r % 2 == 0) ? new Color(25, 32, 48) : new Color(18, 24, 38);

            for (int c = 0; c < numCols; c++) {
                XSLFTableCell cell = dataRow.getCells().get(c);
                cell.setFillColor(rowBg);
                cell.setBorderColor(org.apache.poi.sl.usermodel.TableCell.BorderEdge.bottom, new Color(255, 255, 255, 25));
                cell.setBorderWidth(org.apache.poi.sl.usermodel.TableCell.BorderEdge.bottom, 0.5);

                XSLFTextParagraph p = cell.getTextParagraphs().isEmpty() ? cell.addNewTextParagraph() : cell.getTextParagraphs().get(0);
                XSLFTextRun tr = p.addNewTextRun();
                tr.setText(c < rowData.size() ? rowData.get(c) : "");
                tr.setFontSize(10.0);
                tr.setFontColor(rgb(t.bodyColor()));
                tr.setFontFamily(t.bodyFont());
            }
        }

        // Bottom Highlight Box (Research Gap / Key Insight)
        if (hasGapNote > 0) {
            int noteY = tableY + tableH + 10;
            XSLFAutoShape gapCard = slide.createAutoShape();
            gapCard.setShapeType(ShapeType.ROUND_RECT);
            gapCard.setAnchor(new Rectangle(45, noteY, 870, 45));
            setFill(gapCard, t.bgAccentColor());
            gapCard.setLineColor(rgb(t.accentColor()));

            XSLFTextBox gapText = slide.createTextBox();
            gapText.setAnchor(new Rectangle(58, noteY + 7, 844, 32));
            XSLFTextParagraph gp = gapText.getTextParagraphs().get(0);
            XSLFTextRun gr = gp.addNewTextRun();
            gr.setText(sc.calloutNote());
            gr.setFontSize(10.5);
            gr.setBold(true);
            gr.setFontColor(rgb(t.titleColor()));
            gr.setFontFamily(t.bodyFont());
        }
    }

    // ── 5. ALGORITHM FLOW SLIDE ──────────────────────────────────────────────
    private void renderAlgorithmFlowSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        // 1. Left: Inputs Box
        int inX = 45, inY = 95, inW = 220;
        XSLFAutoShape inHeader = slide.createAutoShape();
        inHeader.setShapeType(ShapeType.ROUND_RECT);
        inHeader.setAnchor(new Rectangle(inX, inY, inW, 32));
        setFill(inHeader, t.bgAccentColor());
        inHeader.setLineColor(rgb(t.accentColor()));

        XSLFTextBox inTitle = slide.createTextBox();
        inTitle.setAnchor(new Rectangle(inX, inY + 4, inW, 24));
        XSLFTextParagraph itp = inTitle.getTextParagraphs().get(0);
        itp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun itr = itp.addNewTextRun();
        itr.setText("Key Inputs & Catalysts");
        itr.setBold(true);
        itr.setFontSize(11.0);
        itr.setFontColor(rgb(t.accentColor()));

        List<String> inputs = sc.algorithmInputs() != null ? sc.algorithmInputs() : List.of("Primary Parameter", "Secondary Variable", "Context Signal", "Operational State");
        int itemY = inY + 42;
        for (String inp : inputs) {
            XSLFAutoShape item = slide.createAutoShape();
            item.setShapeType(ShapeType.ROUND_RECT);
            item.setAnchor(new Rectangle(inX, itemY, inW, 38));
            setFill(item, t.bgAccentColor());
            item.setLineColor(new Color(255, 255, 255, 30));

            XSLFTextBox itemText = slide.createTextBox();
            itemText.setAnchor(new Rectangle(inX + 8, itemY + 6, inW - 16, 26));
            XSLFTextParagraph ip = itemText.getTextParagraphs().get(0);
            XSLFTextRun ir = ip.addNewTextRun();
            ir.setText("•  " + inp);
            ir.setFontSize(10.0);
            ir.setFontColor(rgb(t.bodyColor()));
            itemY += 44;
        }

        // Arrow 1
        XSLFTextBox arr1 = slide.createTextBox();
        arr1.setAnchor(new Rectangle(280, 220, 45, 45));
        XSLFTextRun ar1r = arr1.getTextParagraphs().get(0).addNewTextRun();
        ar1r.setText("➔");
        ar1r.setFontSize(26.0);
        ar1r.setFontColor(rgb(t.accentColor()));

        // 2. Center: Core Engine Box
        int coreX = 335, coreY = 140, coreW = 270, coreH = 220;
        XSLFAutoShape coreBox = slide.createAutoShape();
        coreBox.setShapeType(ShapeType.ROUND_RECT);
        coreBox.setAnchor(new Rectangle(coreX, coreY, coreW, coreH));
        setFill(coreBox, t.bgAccentColor());
        coreBox.setLineColor(rgb(t.accentColor()));

        XSLFTextBox coreText = slide.createTextBox();
        coreText.setAnchor(new Rectangle(coreX + 12, coreY + 30, coreW - 24, coreH - 50));
        XSLFTextParagraph cp = coreText.getTextParagraphs().get(0);
        cp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun cr = cp.addNewTextRun();
        cr.setText((sc.algorithmCoreName() != null ? sc.algorithmCoreName() : "CORE INFERENCE ENGINE") + "\n\n");
        cr.setFontSize(22.0);
        cr.setBold(true);
        cr.setFontColor(rgb(t.titleColor()));

        XSLFTextParagraph csub = coreText.addNewTextParagraph();
        csub.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun csubr = csub.addNewTextRun();
        csubr.setText(sc.algorithmCoreDesc() != null ? sc.algorithmCoreDesc() : "Algorithmic scoring pipeline\nHigher score = critical priority");
        csubr.setFontSize(11.5);
        csubr.setFontColor(rgb(t.accentColor()));

        // Arrow 2
        XSLFTextBox arr2 = slide.createTextBox();
        arr2.setAnchor(new Rectangle(615, 220, 45, 45));
        XSLFTextRun ar2r = arr2.getTextParagraphs().get(0).addNewTextRun();
        ar2r.setText("➔");
        ar2r.setFontSize(26.0);
        ar2r.setFontColor(rgb(t.accentColor()));

        // 3. Right: Risk Tiers
        int tierX = 665, tierY = 95, tierW = 250;
        XSLFAutoShape tierHeader = slide.createAutoShape();
        tierHeader.setShapeType(ShapeType.ROUND_RECT);
        tierHeader.setAnchor(new Rectangle(tierX, tierY, tierW, 32));
        setFill(tierHeader, t.bgAccentColor());
        tierHeader.setLineColor(rgb(t.accentColor()));

        XSLFTextBox tierTitle = slide.createTextBox();
        tierTitle.setAnchor(new Rectangle(tierX, tierY + 4, tierW, 24));
        XSLFTextParagraph ttp = tierTitle.getTextParagraphs().get(0);
        ttp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun ttr = ttp.addNewTextRun();
        ttr.setText("Phases & Classifications");
        ttr.setBold(true);
        ttr.setFontSize(11.0);
        ttr.setFontColor(rgb(t.accentColor()));

        List<String> tiers = sc.algorithmTiers() != null ? sc.algorithmTiers() : List.of("Phase 1: Initial Baseline", "Phase 2: Active Evaluation", "Phase 3: Elevated Priority", "Phase 4: Critical Climax");
        Color[] tierColors = { new Color(46, 204, 113), new Color(241, 196, 15), new Color(230, 126, 34), new Color(231, 76, 60) };

        int tY = tierY + 42;
        for (int i = 0; i < tiers.size(); i++) {
            XSLFAutoShape tierCard = slide.createAutoShape();
            tierCard.setShapeType(ShapeType.ROUND_RECT);
            tierCard.setAnchor(new Rectangle(tierX, tY, tierW, 44));
            Color bgC = i < tierColors.length ? tierColors[i] : t.accentColor() != null ? rgb(t.accentColor()) : Color.DARK_GRAY;
            tierCard.setFillColor(bgC);
            tierCard.setLineColor(null);

            XSLFTextBox tb = slide.createTextBox();
            tb.setAnchor(new Rectangle(tierX + 10, tY + 8, tierW - 20, 28));
            XSLFTextParagraph p = tb.getTextParagraphs().get(0);
            p.setTextAlign(TextParagraph.TextAlign.CENTER);
            XSLFTextRun r = p.addNewTextRun();
            r.setText(tiers.get(i));
            r.setFontSize(11.0);
            r.setBold(true);
            r.setFontColor(Color.WHITE);

            tY += 52;
        }

        // Bottom formula note
        if (sc.calloutNote() != null) {
            XSLFTextBox fn = slide.createTextBox();
            fn.setAnchor(new Rectangle(45, 458, 870, 30));
            XSLFTextParagraph fnp = fn.getTextParagraphs().get(0);
            XSLFTextRun fnr = fnp.addNewTextRun();
            fnr.setText(sc.calloutNote());
            fnr.setFontSize(10.0);
            fnr.setItalic(true);
            fnr.setFontColor(new Color(180, 190, 210));
        }
    }

    // ── 6. METRICS DETAIL SLIDE ──────────────────────────────────────────────
    private void renderMetricsDetailSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        List<String> values = sc.statValues() != null ? sc.statValues() : List.of("92", "25%", "350 / 500", "₹5,25,000");
        List<String> labels = sc.statLabels() != null ? sc.statLabels() : List.of("Score", "Discount", "Sales", "Recovered");

        // Top 4 Metric Boxes
        int startX = 45, y = 95, gap = 18;
        int boxW = (870 - 3 * gap) / 4; // 204
        int boxH = 110;

        for (int i = 0; i < Math.min(4, values.size()); i++) {
            int x = startX + i * (boxW + gap);
            XSLFAutoShape card = slide.createAutoShape();
            card.setShapeType(ShapeType.ROUND_RECT);
            card.setAnchor(new Rectangle(x, y, boxW, boxH));
            setFill(card, t.bgAccentColor());
            card.setLineColor(rgb(t.accentColor()));

            XSLFTextBox text = slide.createTextBox();
            text.setAnchor(new Rectangle(x + 8, y + 12, boxW - 16, boxH - 24));

            XSLFTextParagraph vp = text.getTextParagraphs().get(0);
            vp.setTextAlign(TextParagraph.TextAlign.CENTER);
            XSLFTextRun vr = vp.addNewTextRun();
            vr.setText(values.get(i) + "\n");
            vr.setFontSize(24.0);
            vr.setBold(true);
            vr.setFontColor(rgb(t.accentColor()));
            vr.setFontFamily(t.titleFont());

            XSLFTextParagraph lp = text.addNewTextParagraph();
            lp.setTextAlign(TextParagraph.TextAlign.CENTER);
            XSLFTextRun lr = lp.addNewTextRun();
            lr.setText(labels.get(i));
            lr.setFontSize(10.5);
            lr.setFontColor(rgb(t.bodyColor()));
            lr.setFontFamily(t.bodyFont());
        }

        // Bottom Outcome Bullets
        XSLFTextBox outcomeBox = slide.createTextBox();
        outcomeBox.setAnchor(new Rectangle(45, 225, 870, 260));
        if (sc.bullets() != null) {
            boolean first = true;
            for (String b : sc.bullets()) {
                XSLFTextParagraph p = first ? outcomeBox.getTextParagraphs().get(0) : outcomeBox.addNewTextParagraph();
                first = false;
                p.setSpaceAfter(12.0);
                p.setIndent(8.0);

                XSLFTextRun dot = p.addNewTextRun();
                dot.setText("•  ");
                dot.setFontColor(rgb(t.accentColor()));
                dot.setFontSize(13.0);
                dot.setBold(true);

                XSLFTextRun tr = p.addNewTextRun();
                tr.setText(b);
                tr.setFontColor(rgb(t.bodyColor()));
                tr.setFontSize(12.0);
                tr.setFontFamily(t.bodyFont());
            }
        }
    }

    // ── 7. REFERENCES SLIDE ──────────────────────────────────────────────────
    private void renderReferencesSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        XSLFTextBox refBox = slide.createTextBox();
        refBox.setAnchor(new Rectangle(45, 95, 870, 390));
        if (sc.bullets() != null) {
            boolean first = true;
            for (String ref : sc.bullets()) {
                XSLFTextParagraph p = first ? refBox.getTextParagraphs().get(0) : refBox.addNewTextParagraph();
                first = false;
                p.setSpaceAfter(12.0);

                XSLFTextRun tr = p.addNewTextRun();
                tr.setText(ref);
                tr.setFontColor(rgb(t.bodyColor()));
                tr.setFontSize(11.0);
                tr.setFontFamily(t.bodyFont());
            }
        }
    }

    // ── 8. THANK YOU SLIDE ───────────────────────────────────────────────────
    private void renderThankYouSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(80, 160, 800, 200));

        XSLFTextParagraph tp = box.getTextParagraphs().get(0);
        tp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun tr = tp.addNewTextRun();
        tr.setText("Thank You\n\n");
        tr.setFontSize(42.0);
        tr.setBold(true);
        tr.setFontColor(rgb(t.titleColor()));
        tr.setFontFamily(t.titleFont());

        XSLFTextParagraph sp = box.addNewTextParagraph();
        sp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun sr = sp.addNewTextRun();
        sr.setText(sc.subtitle() != null ? sc.subtitle() : "Questions & Technical Discussion");
        sr.setFontSize(17.0);
        sr.setItalic(true);
        sr.setFontColor(rgb(t.accentColor()));
        sr.setFontFamily(t.bodyFont());
    }

    // ── Standard Content & Fallback Slides ────────────────────────────────────
    private void renderContentSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        XSLFTextBox content = slide.createTextBox();
        content.setAnchor(new Rectangle(45, 95, 870, 390));
        if (sc.bullets() != null && !sc.bullets().isEmpty()) {
            boolean first = true;
            for (String bullet : sc.bullets()) {
                XSLFTextParagraph bp = first ? content.getTextParagraphs().get(0) : content.addNewTextParagraph();
                first = false;
                bp.setSpaceAfter(14.0);
                bp.setIndent(8.0);

                XSLFTextRun dot = bp.addNewTextRun();
                dot.setText("•  ");
                dot.setFontColor(rgb(t.accentColor()));
                dot.setFontSize(13.0);
                dot.setBold(true);

                XSLFTextRun br = bp.addNewTextRun();
                br.setText(bullet);
                br.setFontColor(rgb(t.bodyColor()));
                br.setFontSize(12.5);
                br.setFontFamily(t.bodyFont());
            }
        }
    }

    // ── AGENDA / TABLE OF CONTENTS SLIDE ──────────────────────────────────────
    private void renderAgendaSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title() != null ? sc.title() : "Content", t);

        List<String> items = sc.bullets() != null ? sc.bullets() : (sc.cardTitles() != null ? sc.cardTitles() : List.of());
        int total = items.size();
        int mid = (total + 1) / 2;

        int colW = 425;
        int startY = 95;
        int rowH = 40;
        int gapY = 10;

        for (int i = 0; i < total; i++) {
            boolean isLeft = i < mid;
            int colX = isLeft ? 45 : 490;
            int rowIndex = isLeft ? i : (i - mid);
            int itemY = startY + (rowIndex * (rowH + gapY));

            XSLFAutoShape card = slide.createAutoShape();
            card.setShapeType(ShapeType.ROUND_RECT);
            card.setAnchor(new Rectangle(colX, itemY, colW, rowH));
            setFill(card, t.bgAccentColor());
            card.setLineColor(new Color(255, 255, 255, 20));

            XSLFTextBox tb = slide.createTextBox();
            tb.setAnchor(new Rectangle(colX + 12, itemY + 6, colW - 24, rowH - 12));
            XSLFTextParagraph p = tb.getTextParagraphs().get(0);

            XSLFTextRun numR = p.addNewTextRun();
            numR.setText(String.format("%02d.  ", i + 1));
            numR.setFontSize(13.0);
            numR.setBold(true);
            numR.setFontColor(rgb(t.accentColor()));
            numR.setFontFamily(t.titleFont());

            XSLFTextRun titleR = p.addNewTextRun();
            titleR.setText(items.get(i));
            titleR.setFontSize(12.5);
            titleR.setFontColor(rgb(t.titleColor()));
            titleR.setFontFamily(t.bodyFont());
        }
    }

    // ── ABSTRACT SLIDE ────────────────────────────────────────────────────────
    private void renderAbstractSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title() != null ? sc.title() : "Abstract", t);

        XSLFAutoShape card = slide.createAutoShape();
        card.setShapeType(ShapeType.ROUND_RECT);
        card.setAnchor(new Rectangle(45, 95, 870, 390));
        setFill(card, t.bgAccentColor());
        card.setLineColor(rgb(t.accentColor()));

        XSLFTextBox tb = slide.createTextBox();
        tb.setAnchor(new Rectangle(75, 120, 810, 340));

        String text = sc.calloutDescription() != null ? sc.calloutDescription() :
                (sc.bullets() != null ? String.join("\n\n", sc.bullets()) : "");

        XSLFTextParagraph p = tb.getTextParagraphs().get(0);
        p.setLineSpacing(130.0);
        XSLFTextRun r = p.addNewTextRun();
        r.setText(text);
        r.setFontSize(13.5);
        r.setFontColor(rgb(t.bodyColor()));
        r.setFontFamily(t.bodyFont());
    }

    // ── SCOPE SLIDE (IN SCOPE vs OUT OF SCOPE) ────────────────────────────────
    private void renderScopeSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title() != null ? sc.title() : "Scope", t);

        int colW = 425, colH = 390, colY = 95;

        // 1. In Scope Card (Left)
        XSLFAutoShape leftCard = slide.createAutoShape();
        leftCard.setShapeType(ShapeType.ROUND_RECT);
        leftCard.setAnchor(new Rectangle(45, colY, colW, colH));
        setFill(leftCard, t.bgAccentColor());
        leftCard.setLineColor(new Color(46, 204, 113, 160));

        XSLFTextBox leftTb = slide.createTextBox();
        leftTb.setAnchor(new Rectangle(65, colY + 16, colW - 40, colH - 32));
        XSLFTextParagraph lp0 = leftTb.getTextParagraphs().get(0);
        XSLFTextRun lTitle = lp0.addNewTextRun();
        lTitle.setText("✔  IN SCOPE\n\n");
        lTitle.setFontSize(15.0);
        lTitle.setBold(true);
        lTitle.setFontColor(new Color(46, 204, 113));
        lTitle.setFontFamily(t.titleFont());

        String leftText = sc.leftColumn() != null ? sc.leftColumn() : "";
        for (String line : leftText.split("\n")) {
            if (line.trim().isEmpty()) continue;
            XSLFTextParagraph p = leftTb.addNewTextParagraph();
            p.setSpaceAfter(10.0);
            p.setIndent(8.0);
            XSLFTextRun r = p.addNewTextRun();
            r.setText(line.trim());
            r.setFontSize(11.0);
            r.setFontColor(rgb(t.bodyColor()));
            r.setFontFamily(t.bodyFont());
        }

        // 2. Out of Scope Card (Right)
        XSLFAutoShape rightCard = slide.createAutoShape();
        rightCard.setShapeType(ShapeType.ROUND_RECT);
        rightCard.setAnchor(new Rectangle(490, colY, colW, colH));
        setFill(rightCard, t.bgAccentColor());
        rightCard.setLineColor(new Color(231, 76, 60, 140));

        XSLFTextBox rightTb = slide.createTextBox();
        rightTb.setAnchor(new Rectangle(510, colY + 16, colW - 40, colH - 32));
        XSLFTextParagraph rp0 = rightTb.getTextParagraphs().get(0);
        XSLFTextRun rTitle = rp0.addNewTextRun();
        rTitle.setText("✖  OUT OF SCOPE\n\n");
        rTitle.setFontSize(15.0);
        rTitle.setBold(true);
        rTitle.setFontColor(new Color(231, 76, 60));
        rTitle.setFontFamily(t.titleFont());

        String rightText = sc.rightColumn() != null ? sc.rightColumn() : "";
        for (String line : rightText.split("\n")) {
            if (line.trim().isEmpty()) continue;
            XSLFTextParagraph p = rightTb.addNewTextParagraph();
            p.setSpaceAfter(10.0);
            p.setIndent(8.0);
            XSLFTextRun r = p.addNewTextRun();
            r.setText(line.trim());
            r.setFontSize(11.0);
            r.setFontColor(rgb(t.bodyColor()));
            r.setFontFamily(t.bodyFont());
        }
    }

    // ── METHODOLOGY SLIDE (Phased Steps) ──────────────────────────────────────
    private void renderMethodologySlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title() != null ? sc.title() : "Proposed Methodology", t);

        List<String> titles = sc.cardTitles() != null ? sc.cardTitles() : List.of();
        List<String> descs = sc.cardDescriptions() != null ? sc.cardDescriptions() : List.of();

        int count = Math.min(titles.size(), descs.size());
        if (count == 0) return;

        int colW = 425;
        int rowH = 105;
        int startY = 95;
        int gapY = 14;

        for (int i = 0; i < count; i++) {
            boolean isLeft = i % 2 == 0;
            int rowIndex = i / 2;
            int colX = isLeft ? 45 : 490;
            int cardY = startY + (rowIndex * (rowH + gapY));

            XSLFAutoShape card = slide.createAutoShape();
            card.setShapeType(ShapeType.ROUND_RECT);
            card.setAnchor(new Rectangle(colX, cardY, colW, rowH));
            setFill(card, t.bgAccentColor());
            card.setLineColor(new Color(255, 255, 255, 25));

            XSLFTextBox tb = slide.createTextBox();
            tb.setAnchor(new Rectangle(colX + 14, cardY + 10, colW - 28, rowH - 20));

            XSLFTextParagraph p0 = tb.getTextParagraphs().get(0);
            XSLFTextRun badge = p0.addNewTextRun();
            badge.setText("Phase " + (i + 1) + ": ");
            badge.setFontSize(12.0);
            badge.setBold(true);
            badge.setFontColor(rgb(t.accentColor()));
            badge.setFontFamily(t.titleFont());

            XSLFTextRun titleR = p0.addNewTextRun();
            titleR.setText(titles.get(i) + "\n");
            titleR.setFontSize(12.0);
            titleR.setBold(true);
            titleR.setFontColor(rgb(t.titleColor()));
            titleR.setFontFamily(t.titleFont());

            XSLFTextParagraph p1 = tb.addNewTextParagraph();
            p1.setSpaceBefore(3.0);
            XSLFTextRun descR = p1.addNewTextRun();
            descR.setText(descs.get(i));
            descR.setFontSize(10.0);
            descR.setFontColor(rgb(t.bodyColor()));
            descR.setFontFamily(t.bodyFont());
        }
    }

    private void renderTwoColumnSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        int colW = 425, colH = 390, colY = 95;

        // Left
        XSLFAutoShape leftBox = slide.createAutoShape();
        leftBox.setShapeType(ShapeType.ROUND_RECT);
        leftBox.setAnchor(new Rectangle(45, colY, colW, colH));
        setFill(leftBox, t.bgAccentColor());
        leftBox.setLineColor(null);

        XSLFTextBox leftText = slide.createTextBox();
        leftText.setAnchor(new Rectangle(65, colY + 16, colW - 40, colH - 32));
        XSLFTextParagraph lp = leftText.getTextParagraphs().get(0);
        XSLFTextRun lr = lp.addNewTextRun();
        lr.setText(sc.leftColumn() != null ? sc.leftColumn() : "");
        lr.setFontColor(rgb(t.bodyColor()));
        lr.setFontSize(11.5);

        // Right
        XSLFAutoShape rightBox = slide.createAutoShape();
        rightBox.setShapeType(ShapeType.ROUND_RECT);
        rightBox.setAnchor(new Rectangle(490, colY, colW, colH));
        setFill(rightBox, t.bgAccentColor());
        rightBox.setLineColor(null);

        XSLFTextBox rightText = slide.createTextBox();
        rightText.setAnchor(new Rectangle(510, colY + 16, colW - 40, colH - 32));
        XSLFTextParagraph rp = rightText.getTextParagraphs().get(0);
        XSLFTextRun rr = rp.addNewTextRun();
        rr.setText(sc.rightColumn() != null ? sc.rightColumn() : "");
        rr.setFontColor(rgb(t.bodyColor()));
        rr.setFontSize(11.5);
    }

    private void renderQuoteSlide(XSLFSlide slide, SlideContent sc, ThemeConfig t) {
        addSlideHeader(slide, sc.title(), t);

        XSLFAutoShape quoteCard = slide.createAutoShape();
        quoteCard.setShapeType(ShapeType.ROUND_RECT);
        quoteCard.setAnchor(new Rectangle(90, 150, 780, 210));
        setFill(quoteCard, t.bgAccentColor());
        quoteCard.setLineColor(rgb(t.accentColor()));

        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(120, 175, 720, 160));

        XSLFTextParagraph qp = box.getTextParagraphs().get(0);
        XSLFTextRun qr = qp.addNewTextRun();
        qr.setText("\"" + (sc.quote() != null ? sc.quote() : "") + "\"\n\n");
        qr.setFontSize(17.0);
        qr.setItalic(true);
        qr.setFontColor(rgb(t.titleColor()));

        XSLFTextParagraph ap = box.addNewTextParagraph();
        ap.setTextAlign(TextParagraph.TextAlign.RIGHT);
        XSLFTextRun ar = ap.addNewTextRun();
        ar.setText("— " + (sc.quoteAuthor() != null ? sc.quoteAuthor() : ""));
        ar.setFontSize(13.0);
        ar.setFontColor(rgb(t.accentColor()));
    }

    // ── Helper Utilities ──────────────────────────────────────────────────────
    private void addSlideHeader(XSLFSlide slide, String titleText, ThemeConfig t) {
        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(45, 28, 870, 48));
        XSLFTextParagraph p = box.getTextParagraphs().get(0);
        XSLFTextRun r = p.addNewTextRun();
        r.setText(titleText);
        r.setFontSize(24.0);
        r.setBold(true);
        r.setFontColor(rgb(t.titleColor()));
        r.setFontFamily(t.titleFont());

        // Accent strip under title
        XSLFAutoShape strip = slide.createAutoShape();
        strip.setShapeType(ShapeType.RECT);
        strip.setAnchor(new Rectangle(45, 78, 55, 3));
        setFill(strip, t.accentColor());
        strip.setLineColor(null);
    }

    private void addRunningFooter(XSLFSlide slide, String runningHeader, int slideNum, ThemeConfig t) {
        XSLFTextBox footer = slide.createTextBox();
        footer.setAnchor(new Rectangle(45, 502, 800, 24));

        // Left running text
        XSLFTextParagraph p = footer.getTextParagraphs().get(0);
        XSLFTextRun lr = p.addNewTextRun();
        lr.setText(runningHeader != null ? runningHeader : "LKYslides Academic Presentation");
        lr.setFontSize(9.0);
        lr.setFontColor(new Color(130, 140, 160));
        lr.setFontFamily(t.bodyFont());

        // Right slide number
        XSLFTextBox numBox = slide.createTextBox();
        numBox.setAnchor(new Rectangle(855, 502, 60, 24));
        XSLFTextParagraph np = numBox.getTextParagraphs().get(0);
        np.setTextAlign(TextParagraph.TextAlign.RIGHT);
        XSLFTextRun nr = np.addNewTextRun();
        nr.setText(String.valueOf(slideNum));
        nr.setFontSize(9.0);
        nr.setFontColor(new Color(130, 140, 160));
        nr.setFontFamily(t.bodyFont());
    }

    private void clearSlideBackground(XSLFSlide slide, ThemeConfig theme) {
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setShapeType(ShapeType.RECT);
        bg.setAnchor(new Rectangle(0, 0, SLIDE_WIDTH, SLIDE_HEIGHT));
        setFill(bg, theme.bgColor());
        bg.setLineColor(null);
    }

    private void addSpeakerNote(XSLFSlide slide, String noteText) {
        if (noteText == null || noteText.isBlank()) return;
        try {
            XSLFNotes notes = slide.getNotes();
            if (notes != null) {
                for (XSLFTextShape shape : notes.getPlaceholders()) {
                    if (shape.getTextType() == org.apache.poi.sl.usermodel.Placeholder.BODY) {
                        shape.setText(noteText);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void setFill(XSLFAutoShape shape, int[] rgb) {
        shape.setFillColor(rgb(rgb));
    }

    private Color rgb(int[] arr) {
        if (arr == null || arr.length < 3) return Color.WHITE;
        return new Color(arr[0], arr[1], arr[2]);
    }
}