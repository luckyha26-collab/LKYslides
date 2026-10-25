package com.slidegen.engine;

import com.slidegen.model.OutlineCard;
import com.slidegen.model.PresentationRequest;
import com.slidegen.model.SlideContent;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ContentHumanizer: Natural, humanized presentation synthesis engine for LKYslides.
 * Crafts presentations tailored to the real academic and project presentation standard:
 * Title ➔ Content (Agenda) ➔ Abstract ➔ Problem Statement ➔ Objectives ➔
 * Scope (In-Scope vs Out-of-Scope) ➔ Proposed Methodology ➔ Technology Stack ➔
 * Expected Outcome ➔ Future Scope ➔ References ➔ Thank You.
 *
 * Automatically parses user-provided content sections if pasted, or dynamically
 * synthesizes thoughtful, domain-authentic copy for any topic.
 */
@Service
public class ContentHumanizer {

    public enum Domain {
        AI_ENGINEERING,
        BIOMEDICAL_HEALTHCARE,
        ENVIRONMENT_ENERGY,
        BUSINESS_STARTUP,
        PHYSICS_AEROSPACE,
        HISTORY_HUMANITIES,
        GENERAL_ACADEMIC
    }

    /**
     * Builds interactive outline cards dynamically for the user's topic and requested slides.
     */
    public List<OutlineCard> buildOutline(PresentationRequest req) {
        List<SlideContent> slides = buildSlides(req);
        List<OutlineCard> outline = new ArrayList<>();

        for (int i = 0; i < slides.size(); i++) {
            SlideContent s = slides.get(i);
            String summary;
            List<String> keyPoints = new ArrayList<>();

            switch (s.layoutType()) {
                case "TITLE_SLIDE" -> {
                    summary = "Title slide establishing scope, authors, and supervisor.";
                    if (s.teamMembers() != null && !s.teamMembers().isEmpty()) {
                        keyPoints.addAll(s.teamMembers());
                    } else if (req.presentedBy() != null && !req.presentedBy().isBlank()) {
                        keyPoints.add("Speaker: " + req.presentedBy());
                    }
                    if (s.projectGuide() != null && !s.projectGuide().isBlank()) {
                        keyPoints.add("Supervisor: " + s.projectGuide());
                    }
                }
                case "AGENDA" -> {
                    summary = "Table of contents / presentation roadmap.";
                    if (s.bullets() != null) keyPoints.addAll(s.bullets().stream().limit(4).toList());
                }
                case "ABSTRACT" -> {
                    summary = "Executive abstract summarizing the motivation, core problem, and proposed solution.";
                    String desc = s.calloutDescription() != null ? s.calloutDescription() : "";
                    keyPoints.add(desc.length() > 90 ? desc.substring(0, 87) + "..." : desc);
                }
                case "BULLETS" -> {
                    summary = "Detailed structured analysis covering core constraints or objectives.";
                    if (s.bullets() != null) keyPoints.addAll(s.bullets().stream().limit(3).toList());
                }
                case "SCOPE" -> {
                    summary = "Project boundaries distinguishing In-Scope deliverables from Out-of-Scope limits.";
                    keyPoints.add("In Scope: Core platform features & modules");
                    keyPoints.add("Out of Scope: External hardware & third-party certifications");
                }
                case "METHODOLOGY" -> {
                    summary = "Phased engineering methodology from literature review to testing.";
                    if (s.cardTitles() != null) keyPoints.addAll(s.cardTitles().stream().limit(3).toList());
                }
                case "TABLE" -> {
                    summary = "Structured categorized framework / technology stack / comparative analysis.";
                    if (s.tableHeaders() != null) keyPoints.add("Categories: " + String.join(" | ", s.tableHeaders()));
                }
                case "REFERENCES" -> {
                    summary = "Peer-reviewed literature citations and scholarly bibliography.";
                    keyPoints.add("Formal academic references & publications");
                }
                case "THANK_YOU" -> {
                    summary = "Closing acknowledgment, questions, and technical discussion.";
                    keyPoints.add("Open floor for evaluator Q&A");
                }
                default -> {
                    summary = "Technical evaluation and project details.";
                    if (s.bullets() != null) keyPoints.addAll(s.bullets().stream().limit(2).toList());
                }
            }

            outline.add(new OutlineCard(i + 1, s.title(), s.layoutType(), summary, keyPoints));
        }

        return outline;
    }

    /**
     * Builds a comprehensive presentation customized dynamically to the given topic and requested slide count.
     */
    public List<SlideContent> buildSlides(PresentationRequest req) {
        String topic = req.topic() != null ? req.topic().trim() : "Project Presentation";
        String content = req.content() != null ? req.content().trim() : "";
        String inst = req.effectiveInstitution();
        String dept = req.effectiveDepartment();
        List<String> team = req.effectiveTeamMembers();
        String guide = req.effectiveGuide();
        int targetCount = req.slideCount() != null ? req.slideCount() : 10;
        targetCount = Math.max(4, Math.min(25, targetCount));

        // 1. Try to parse user-provided structured sections if they pasted custom notes/syllabus
        Map<String, List<String>> userSections = parseUserSections(content);

        // 2. Identify domain archetype
        Domain domain = detectDomain(topic, content);

        // 3. Synthesize the presentation blueprint tailored to the domain and user input
        List<SlideBlueprint> masterSuite = buildMasterSuite(topic, userSections, domain, req);

        // 4. Select the optimal subset matching targetCount and sort into natural presentation order
        return selectAndRenumber(masterSuite, targetCount, topic, inst, dept, team, guide);
    }

    // ── User Content Section Parser ─────────────────────────────────────────────

    private Map<String, List<String>> parseUserSections(String raw) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return sections;

        String[] lines = raw.split("\n");
        String currentSection = null;
        List<String> currentLines = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile("^(content|abstract|problem statement|objectives|scope|in scope|out of scope|proposed methodology|methodology|technology stack|tech stack|expected outcome|expected outcomes|future scope|references|conclusion)", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Check if line is a section header
            String lower = trimmed.toLowerCase().replaceAll("[:\\-#*]+$", "").trim();
            Matcher m = sectionPattern.matcher(lower);

            if (m.find() && lower.length() < 35) {
                if (currentSection != null && !currentLines.isEmpty()) {
                    sections.put(currentSection, new ArrayList<>(currentLines));
                }
                currentSection = m.group(1).toLowerCase();
                currentLines.clear();
            } else if (currentSection != null) {
                // Remove leading bullet symbols
                String cleanLine = trimmed.replaceAll("^[-*•\\d.]+\\s*", "").trim();
                if (!cleanLine.isEmpty()) {
                    currentLines.add(cleanLine);
                }
            }
        }

        if (currentSection != null && !currentLines.isEmpty()) {
            sections.put(currentSection, currentLines);
        }

        return sections;
    }

    // ── Domain Detection ─────────────────────────────────────────────────────────

    public Domain detectDomain(String topic, String content) {
        String t = topic.toLowerCase();
        if (matchesAny(t, "retina", "cancer", "medical", "medicine", "patient", "clinical", "disease",
                "genetics", "crispr", "dna", "fundus", "hospital", "pharma", "drug", "therapy", "surgery",
                "vaccine", "cellular", "pathology", "tumor", "anatomy", "cardio", "ophthalmology", "diabetic")) {
            return Domain.BIOMEDICAL_HEALTHCARE;
        }

        if (matchesAny(t, "robot", "slam", "drone", "uav", "ai", "machine learning", "deep learning",
                "cnn", "vision", "neural", "algorithm", "software", "cyber", "security", "cloud", "database",
                "blockchain", "crypto", "iot", "compiler", "operating system", "llm", "nlp", "dataset", "microservice")) {
            return Domain.AI_ENGINEERING;
        }

        if (matchesAny(t, "climate", "carbon", "renewable", "solar", "wind", "hydrogen", "energy",
                "emission", "green", "battery", "grid", "ecology", "sustainability", "pollution", "ev",
                "electric vehicle", "fossil", "nuclear", "decarbonization")) {
            return Domain.ENVIRONMENT_ENERGY;
        }

        if (matchesAny(t, "pitch", "startup", "saas", "business", "market", "revenue", "finance",
                "fintech", "customer", "b2b", "sales", "roi", "cac", "ltv", "venture", "investor", "retail", "ecommerce")) {
            return Domain.BUSINESS_STARTUP;
        }

        if (matchesAny(t, "quantum", "physics", "relativity", "astro", "space", "rocket", "cosmology",
                "particle", "thermodynamics", "optics", "laser", "gravity", "satellite", "orbital", "fusion")) {
            return Domain.PHYSICS_AEROSPACE;
        }

        if (matchesAny(t, "history", "empire", "war", "revolution", "roman", "medieval", "ancient",
                "civilization", "renaissance", "monarchy", "dynasty", "archaeology", "historian")) {
            return Domain.HISTORY_HUMANITIES;
        }

        return Domain.GENERAL_ACADEMIC;
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String k : keywords) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(k.toLowerCase()) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) return true;
        }
        return false;
    }

    private String cleanTopic(String topic) {
        return topic.replaceAll("(?i)^(a|an|the|study on|research on|overview of)\\s+", "").trim();
    }

    // ── Slide Blueprint Record ───────────────────────────────────────────────────

    public record SlideBlueprint(
            int priority,              // 1 = highest essential, 14 = optional deep dive
            int narrativePosition,     // Natural presentation sequence (1 to 14)
            String layoutType,
            String title,
            String subtitle,
            List<String> bullets,
            String leftColumn,
            String rightColumn,
            String calloutMetric,
            String calloutLabel,
            String calloutDesc,
            List<String> cardTitles,
            List<String> cardDescriptions,
            List<String> tableHeaders,
            List<List<String>> tableRows,
            String calloutNote,
            String speakerNote
    ) {}

    // ── Master Presentation Suite Builder ────────────────────────────────────────

    private List<SlideBlueprint> buildMasterSuite(
            String topic,
            Map<String, List<String>> userSections,
            Domain domain,
            PresentationRequest req
    ) {
        String clean = cleanTopic(topic);
        List<SlideBlueprint> list = new ArrayList<>();

        // ── 1. TITLE SLIDE ────────────────────────────────────────────────────────
        list.add(new SlideBlueprint(
                1, 1, "TITLE_SLIDE", topic,
                subtitleForDomain(topic, domain),
                null, null, null, null, null, null, null, null, null, null, null,
                "Introduce the project title, team members, supervisor, and overall project goal."
        ));

        // ── 2. CONTENT / AGENDA ───────────────────────────────────────────────────
        List<String> agendaItems = List.of(
                "Abstract & Overview",
                "Problem Statement",
                "Project Objectives",
                "Project Scope (In-Scope & Out-of-Scope)",
                "Proposed Methodology & Architecture",
                "Technology Stack & Implementation",
                "Expected Outcomes & Deliverables",
                "Future Scope & Extensions",
                "Academic References & Bibliography"
        );
        list.add(new SlideBlueprint(
                2, 2, "AGENDA", "Content", null,
                agendaItems, null, null, null, null, null, agendaItems, null, null, null, null,
                "Provide evaluators with a clear roadmap of the presentation sections."
        ));

        // ── 3. ABSTRACT ───────────────────────────────────────────────────────────
        String abstractText;
        if (userSections.containsKey("abstract") && !userSections.get("abstract").isEmpty()) {
            abstractText = String.join(" ", userSections.get("abstract"));
        } else {
            abstractText = defaultAbstract(clean, domain);
        }
        list.add(new SlideBlueprint(
                3, 3, "ABSTRACT", "Abstract", null,
                null, null, null, null, null, abstractText, null, null, null, null, null,
                "Deliver a concise executive summary covering the motivation, technical approach, and core deliverable."
        ));

        // ── 4. PROBLEM STATEMENT ──────────────────────────────────────────────────
        List<String> problemBullets;
        if (userSections.containsKey("problem statement") && !userSections.get("problem statement").isEmpty()) {
            problemBullets = userSections.get("problem statement");
        } else {
            problemBullets = defaultProblemBullets(clean, domain);
        }
        list.add(new SlideBlueprint(
                4, 4, "BULLETS", "Problem Statement", null,
                problemBullets, null, null, null, null, null, null, null, null, null, null,
                "Walk evaluators through the real-world operational challenges and limitations of existing solutions."
        ));

        // ── 5. OBJECTIVES ─────────────────────────────────────────────────────────
        List<String> objectiveBullets;
        if (userSections.containsKey("objectives") && !userSections.get("objectives").isEmpty()) {
            objectiveBullets = userSections.get("objectives");
        } else {
            objectiveBullets = defaultObjectiveBullets(clean, domain);
        }
        list.add(new SlideBlueprint(
                5, 5, "BULLETS", "Objectives", null,
                objectiveBullets, null, null, null, null, null, null, null, null, null, null,
                "Enumerate the exact engineering deliverables and measurable technical milestones."
        ));

        // ── 6. SCOPE (IN SCOPE vs OUT OF SCOPE) ───────────────────────────────────
        List<String> inScope = new ArrayList<>();
        List<String> outOfScope = new ArrayList<>();

        if (userSections.containsKey("scope") || userSections.containsKey("in scope")) {
            List<String> rawScope = userSections.containsKey("scope") ? userSections.get("scope") : userSections.get("in scope");
            boolean inMode = true;
            for (String s : rawScope) {
                if (s.toLowerCase().contains("out of scope")) {
                    inMode = false;
                } else if (s.toLowerCase().contains("in scope")) {
                    inMode = true;
                } else {
                    if (inMode) inScope.add(s);
                    else outOfScope.add(s);
                }
            }
        }
        if (inScope.isEmpty()) {
            inScope = defaultInScope(clean, domain);
            outOfScope = defaultOutOfScope(clean, domain);
        }

        list.add(new SlideBlueprint(
                6, 6, "SCOPE", "Scope", null,
                inScope,
                "• " + String.join("\n• ", inScope),
                "• " + String.join("\n• ", outOfScope),
                null, null, null, null, null, null, null, null,
                "Clearly demarcate project boundaries to demonstrate academic clarity and realistic engineering goals."
        ));

        // ── 7. PROPOSED METHODOLOGY ───────────────────────────────────────────────
        List<String> phaseTitles = new ArrayList<>();
        List<String> phaseDescs = new ArrayList<>();

        if (userSections.containsKey("proposed methodology") || userSections.containsKey("methodology")) {
            List<String> rawM = userSections.containsKey("proposed methodology") ? userSections.get("proposed methodology") : userSections.get("methodology");
            for (String line : rawM) {
                int colonIdx = line.indexOf(':');
                if (colonIdx > 0 && colonIdx < 30) {
                    phaseTitles.add(line.substring(0, colonIdx).trim());
                    phaseDescs.add(line.substring(colonIdx + 1).trim());
                } else {
                    phaseTitles.add("Phase " + (phaseTitles.size() + 1));
                    phaseDescs.add(line);
                }
            }
        }
        if (phaseTitles.isEmpty()) {
            defaultMethodology(clean, domain, phaseTitles, phaseDescs);
        }

        list.add(new SlideBlueprint(
                7, 7, "METHODOLOGY", "Proposed Methodology", null,
                null, null, null, null, null, null,
                phaseTitles, phaseDescs, null, null, null,
                "Explain the phased workflow step by step, from data ingestion to model training and full-stack deployment."
        ));

        // ── 8. TECHNOLOGY STACK ───────────────────────────────────────────────────
        List<String> stackHeaders = List.of("System Component", "Selected Technology / Tool", "Purpose & Architectural Role");
        List<List<String>> stackRows = defaultTechStackRows(clean, domain);

        list.add(new SlideBlueprint(
                8, 8, "TABLE", "Technology Stack", null,
                null, null, null, null, null, null, null, null,
                stackHeaders, stackRows,
                "Architecture Decision: Fully open, scalable, and modular stack with decoupled microservices.",
                "Justify each technology choice based on latency, performance, security, and developer ecosystem."
        ));

        // ── 9. EXPECTED OUTCOME ───────────────────────────────────────────────────
        List<String> outcomeBullets;
        if (userSections.containsKey("expected outcome") || userSections.containsKey("expected outcomes")) {
            outcomeBullets = userSections.containsKey("expected outcome") ? userSections.get("expected outcome") : userSections.get("expected outcomes");
        } else {
            outcomeBullets = defaultExpectedOutcomes(clean, domain);
        }
        list.add(new SlideBlueprint(
                9, 9, "BULLETS", "Expected Outcome", null,
                outcomeBullets, null, null, null, null, null, null, null, null, null, null,
                "Present the tangible working deliverables, prototype capabilities, and benchmark targets."
        ));

        // ── 10. FUTURE SCOPE ──────────────────────────────────────────────────────
        List<String> futureBullets;
        if (userSections.containsKey("future scope")) {
            futureBullets = userSections.get("future scope");
        } else {
            futureBullets = defaultFutureScope(clean, domain);
        }
        list.add(new SlideBlueprint(
                10, 10, "BULLETS", "Future Scope", null,
                futureBullets, null, null, null, null, null, null, null, null, null, null,
                "Demonstrate long-term research vision, production runway, and real-world deployment avenues."
        ));

        // ── 11. REFERENCES ────────────────────────────────────────────────────────
        List<String> references;
        if (userSections.containsKey("references") && !userSections.get("references").isEmpty()) {
            references = userSections.get("references");
        } else {
            references = defaultReferences(clean, domain);
        }
        list.add(new SlideBlueprint(
                11, 11, "REFERENCES", "References", null,
                references, null, null, null, null, null, null, null, null, null, null,
                "Properly cite foundational peer-reviewed literature and benchmark research papers."
        ));

        // ── 12. THANK YOU ─────────────────────────────────────────────────────────
        list.add(new SlideBlueprint(
                12, 12, "THANK_YOU", "Thank You !!",
                clean + " | Questions & Discussion",
                null, null, null, null, null, null, null, null, null, null, null,
                "Acknowledge evaluators and open the floor for technical Q&A."
        ));

        return list;
    }

    // ── Domain Default Content Synthesizers ──────────────────────────────────────

    private String subtitleForDomain(String topic, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> "AI-Powered Clinical Screening & Diagnostic Support Platform";
            case AI_ENGINEERING        -> "Autonomous System Architecture, Deep Learning Pipeline & Full-Stack Platform";
            case ENVIRONMENT_ENERGY    -> "Decarbonization Framework, Renewable Engineering & Techno-Economic Validation";
            case BUSINESS_STARTUP      -> "Commercial Strategy, Scalable Architecture & Market Opportunity";
            case PHYSICS_AEROSPACE     -> "Theoretical Formulation, Precision Instrumentation & Empirical Analysis";
            case HISTORY_HUMANITIES    -> "Historiographical Analysis, Structural Catalysts & Cultural Transformation";
            default                    -> "Comprehensive Academic Framework, Technical Methodology & Evaluation";
        };
    }

    private String defaultAbstract(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE ->
                    clean + " represents a critical domain in modern healthcare, where late diagnosis and specialist shortages exacerbate clinical complications. This project proposes an AI-powered automated screening platform using specialized deep convolutional neural networks to rapidly analyze diagnostic imagery, provide multi-class severity staging, and generate explainable attention heatmaps to deliver fast, consistent clinical decision support through a full-stack web and mobile application.";
            case AI_ENGINEERING ->
                    "Modern computing and intelligent systems increasingly demand autonomous, real-time capability without compromising latency or accuracy. This project introduces a scalable deep-learning platform for " + clean + ", combining specialized neural backbones with an asynchronous inference microservice and responsive full-stack dashboard to provide explainable, high-throughput intelligence for production environments.";
            case ENVIRONMENT_ENERGY ->
                    "Global ecological and climate targets demand radical technological transformation across heavy-emission sectors. This project presents a comprehensive engineering and techno-economic framework for " + clean + ", analyzing lifecycle carbon displacement, renewable vector conversion, and grid integration dynamics to deliver a scalable, economically viable net-zero transition roadmap.";
            case BUSINESS_STARTUP ->
                    "Enterprise efficiency and customer lifetime value are fundamentally bottlenecked by fragmented manual workflows. This project delivers an intelligent, full-stack SaaS platform for " + clean + ", automating predictive churn prevention, self-serve ingestion, and usage-based workflows to capture trapped operational value and expand enterprise margins.";
            case HISTORY_HUMANITIES ->
                    "The historical transformation of " + clean + " represents an epochal convergence of socioeconomic decay, ideological shifts, and external geopolitical catalysts. This study provides an in-depth historiographical investigation analyzing the primary sources, institutional vulnerabilities, and enduring cultural legacies that shaped the transition to modern political and social structures.";
            default ->
                    "The study of " + clean + " requires moving beyond isolated empirical observations toward a unified, rigorous methodological framework. This project presents a structured analytical model combining qualitative domain principles with quantifiable benchmarks, validated through empirical testing and presented via an accessible full-stack interface.";
        };
    }

    private List<String> defaultProblemBullets(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Severe shortage of certified specialists relative to the growing patient population delays timely screening.",
                    "Manual grading and diagnostic inspection is subjective, time-intensive, and prone to significant inter-observer variability.",
                    "Specialized diagnostic infrastructure is concentrated in major urban centers, leaving rural and low-resource communities under-screened.",
                    "Late-stage detection drastically increases the risk of irreversible complications that early proactive intervention could have prevented."
            );
            case AI_ENGINEERING -> List.of(
                    "High inference latency and memory bloat in standard deep learning models limit edge and real-time deployment.",
                    "Black-box neural predictions lack interpretability, reducing operational trust among end users and system evaluators.",
                    "Significant dataset domain shift and field condition variance degrade real-world generalization compared to laboratory benchmarks.",
                    "Lack of unified full-stack integration creates disjointed workflows between ML pipelines and client interfaces."
            );
            case ENVIRONMENT_ENERGY -> List.of(
                    "Heavy dependence on carbon-intensive fossil inputs drives compounding greenhouse gas emissions across the sector.",
                    "Intermittency and seasonal volatility in renewable generation present severe grid balancing and storage challenges.",
                    "High upfront capital expenditure (CapEx) hurdles delay the adoption of clean technology alternatives at commercial scale.",
                    "Fragmented regulatory standards and opaque supply chain telemetry prevent accurate lifecycle carbon accounting."
            );
            case BUSINESS_STARTUP -> List.of(
                    "Enterprises lose millions annually to operational friction, manual administrative overhead, and opaque data silos.",
                    "Traditional software tools provide retrospective reporting rather than proactive, automated real-time intelligence.",
                    "High customer acquisition costs (CAC) paired with preventable churn severely erode net operating margins.",
                    "Complex multi-month implementation cycles hinder rapid enterprise adoption and delay realized return on investment."
            );
            default -> List.of(
                    "Conventional methodologies rely on fragmented, static heuristics that fail to capture dynamic system complexity.",
                    "Lack of standardized, score-based quantitative evaluation leads to subjective decision-making across teams.",
                    "Existing workflows operate in data silos, resulting in delayed interventions and unmitigated risk accumulation.",
                    "Scalability constraints prevent broader institutional deployment under high-concurrency operating demands."
            );
        };
    }

    private List<String> defaultObjectiveBullets(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Build a full-stack platform for automated detection and multi-class severity staging from diagnostic input imagery.",
                    "Develop and benchmark deep neural architectures (custom CNN baseline vs transfer learning) for classification accuracy and generalization.",
                    "Incorporate explainability (Grad-CAM / attention heatmaps) so predictions visually highlight clinically relevant lesion regions.",
                    "Provide confidence scores and prioritized referral recommendations for every prediction to support clinical decision-making.",
                    "Deliver a responsive web/mobile interface for image upload, results visualization, and historical patient telemetry tracking."
            );
            case AI_ENGINEERING -> List.of(
                    "Engineer an end-to-end inference pipeline delivering deterministic sub-second predictions on high-throughput workloads.",
                    "Benchmark multiple deep learning architectures to balance classification accuracy against computational footprint.",
                    "Implement model interpretability layers (attention maps / saliency) to verify feature attribution across edge cases.",
                    "Expose the core model as a containerized REST API microservice with automated request validation and rate limiting.",
                    "Deliver an intuitive, responsive frontend dashboard enabling real-time telemetry monitoring and audit logging."
            );
            case ENVIRONMENT_ENERGY -> List.of(
                    "Quantify the exact baseline carbon footprint and model emission displacement achievable through clean tech substitution.",
                    "Develop a multi-variable techno-economic model calculating Levelized Cost of Energy (LCOE) across transition scenarios.",
                    "Evaluate long-duration storage and energy vector conversion efficiency under dynamic supply-demand cycles.",
                    "Deliver a strategic policy and deployment roadmap aligning with international net-zero decarbonization milestones."
            );
            default -> List.of(
                    "Establish a standardized quantitative framework to evaluate core performance parameters across diverse scenarios.",
                    "Design and implement a modular, high-throughput computational pipeline to automate end-to-end evaluation.",
                    "Validate the proposed approach against published industry benchmarks and historical baseline datasets.",
                    "Deliver a complete, user-friendly digital interface providing transparent reporting and actionable recommendations."
            );
        };
    }

    private List<String> defaultInScope(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Web and mobile application for diagnostic image upload and automated severity classification",
                    "Deep learning pipeline with multi-class grading and calibrated confidence scoring",
                    "Grad-CAM explainability overlays highlighting pathological lesion regions",
                    "Clinician and patient dashboards with screening history and PDF exportable reports",
                    "RESTful API microservice architecture for decoupled, scalable inference"
            );
            case AI_ENGINEERING -> List.of(
                    "Core model training, fine-tuning, and cross-validation across standard benchmark datasets",
                    "Containerized inference server exposed via high-throughput RESTful endpoints",
                    "Feature extraction, preprocessing normalization, and data augmentation pipelines",
                    "Interactive administrative dashboard with live telemetry visualization and user management"
            );
            default -> List.of(
                    "Full-stack software application with responsive user interface and backend business logic",
                    "Mathematical and algorithmic scoring engine with validated parameter thresholds",
                    "Automated reporting, data export, and historical state tracking modules",
                    "REST API architecture enabling seamless third-party data integration"
            );
        };
    }

    private List<String> defaultOutOfScope(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Physical camera or optical hardware sensor manufacturing",
                    "Direct real-time integration with proprietary hospital EMR / PACS systems",
                    "Formal medical-device regulatory certification (FDA / CE mark clinical trial filings)",
                    "Autonomous prescribing or replacing licensed physician diagnostic judgment"
            );
            case AI_ENGINEERING -> List.of(
                    "Custom silicon / ASIC hardware fabrication and low-level firmware authoring",
                    "Autonomous execution on mission-critical hardware without human-in-the-loop oversight",
                    "Full-scale global multi-datacenter geographic replication in prototype phase"
            );
            default -> List.of(
                    "Third-party proprietary enterprise hardware modifications",
                    "Commercial legal licensing agreements and formal compliance audits",
                    "Production-scale enterprise migration across legacy mainframes"
            );
        };
    }

    private void defaultMethodology(String clean, Domain domain, List<String> titles, List<String> descs) {
        titles.addAll(List.of(
                "Requirement Analysis & Literature Review",
                "Data Ingestion & Preprocessing",
                "Model Development & Architecture",
                "Explainability & Visual Verification",
                "Full-Stack Integration & APIs",
                "Testing, Evaluation & Benchmarking"
        ));

        descs.addAll(List.of(
                "Review foundational peer-reviewed literature and identify benchmark open datasets to finalize architecture specifications.",
                "Implement normalization, adaptive contrast enhancement (CLAHE), resizing, and augmentation to ensure field-condition robustness.",
                "Train and compare a custom baseline model against state-of-the-art transfer-learning backbones using optimized loss functions.",
                "Integrate Grad-CAM / attention heatmaps to visualize feature attribution and ensure model predictions are clinically/technically sound.",
                "Expose the trained engine as an independent microservice via RESTful API; connect responsive client dashboards for seamless user interaction.",
                "Benchmark accuracy, quadratic weighted kappa (QWK), precision/recall, AUC-ROC curves, and end-to-end inference latency under peak stress loads."
        ));
    }

    private List<List<String>> defaultTechStackRows(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    List.of("Frontend Client", "HTML5, Modern CSS3, JavaScript (ES6+)", "Responsive clinician portal & patient screening dashboard"),
                    List.of("Backend Server", "Java 21, Spring Boot 3 / Python FastAPI", "High-concurrency microservice, REST API, request validation"),
                    List.of("AI & ML Core", "PyTorch / TensorFlow, torchvision, OpenCV", "Deep CNN model training, transfer learning, tensor evaluation"),
                    List.of("Explainability", "Grad-CAM, Grad-CAM++, Matplotlib", "Generates lesion attention heatmaps for clinical transparency"),
                    List.of("Datasets", "APTOS 2019, EyePACS, Messidor-2, IDRiD", "Standardized benchmark datasets for training and cross-validation"),
                    List.of("Persistence & Docs", "PostgreSQL / SQLite, Apache POI", "Secure telemetry records, audit logging, and report compilation")
            );
            case AI_ENGINEERING -> List.of(
                    List.of("Client Layer", "Modern Responsive Web UI, Canvas Engine", "Interactive real-time visualization & telemetry monitoring"),
                    List.of("API Gateway", "Spring Boot 3 RESTful Endpoints", "Rate limiting, CORS orchestration, stateless token authentication"),
                    List.of("Compute Engine", "Java 21 Virtual Threads & Concurrency", "High-throughput asynchronous event handling & business logic"),
                    List.of("ML & Inference", "PyTorch C++ / ONNX Runtime / CUDA", "Optimized tensor vectorization and hardware-accelerated scoring"),
                    List.of("Storage Layer", "Relational Database (ACID) & Cache", "Transactional persistence, indexed telemetry, audit logging")
            );
            default -> List.of(
                    List.of("Presentation UI", "HTML5, Modern CSS Grid, JavaScript", "Lightweight, cross-platform interface without external bloat"),
                    List.of("Application Server", "Java 21, Spring Boot 3 Framework", "Enterprise-grade microservice with low latency and type safety"),
                    List.of("Computational Core", "Mathematical & Algorithmic Modules", "Evaluates weighted input vectors and normalizes scoring metrics"),
                    List.of("Document Pipeline", "Apache POI 5.2.5 (Native OOXML)", "Generates 100% watermark-free presentations directly on server"),
                    List.of("Database Layer", "Relational Store with Transactional Log", "Reliable data persistence, indexing, and verifiable audit records")
            );
        };
    }

    private List<String> defaultExpectedOutcomes(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Fully functional working prototype delivering automated multi-class severity grading with calibrated confidence scores.",
                    "Deep learning models achieving accuracy and quadratic weighted kappa (QWK) consistent with published clinical benchmarks (~85–96%).",
                    "Explainable Grad-CAM heatmap overlays highlighting clinically relevant pathological lesion regions for every prediction.",
                    "Integrated screening dashboard with complete patient history tracking and exportable diagnostic evaluation summaries."
            );
            case AI_ENGINEERING -> List.of(
                    "Production-ready prototype delivering deterministic, low-latency predictions (< 15ms) under heavy concurrency.",
                    "Empirical accuracy exceeding 94% across cross-validation tests on standard benchmark evaluation sets.",
                    "Interpretable visual feature attribution validating that the model does not rely on spurious correlation artifacts.",
                    "Documented RESTful API suite ready for seamless integration into downstream enterprise microservices."
            );
            default -> List.of(
                    "Complete, validated software prototype achieving all core functional and technical specifications.",
                    "Quantifiable performance gains demonstrating significant speedup and error reduction compared to legacy baselines.",
                    "Intuitive, role-based dashboards enabling non-technical stakeholders to evaluate metrics effortlessly.",
                    "Comprehensive evaluation report documenting stress testing, boundary constraints, and edge-case verifications."
            );
        };
    }

    private List<String> defaultFutureScope(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "Integration with portable hardware attachments and smartphone-based imaging tools for rural field screening camps.",
                    "Multi-disease extension (e.g. glaucoma, macular edema, AMD) leveraging the same core diagnostic imaging pipeline.",
                    "Federated learning architecture across distributed hospitals to continuously improve models while preserving patient data privacy.",
                    "Partnership and integration with government public health screening initiatives and telemedicine platforms for real-world deployment."
            );
            case AI_ENGINEERING -> List.of(
                    "Edge hardware porting targeting embedded FPGA/ASIC accelerators for ultra-low power execution in disconnected environments.",
                    "Integration of self-supervised foundation models to reduce reliance on large-scale manually labeled training data.",
                    "Decentralized federated coordination across multi-node edge deployments with encrypted state synchronization.",
                    "Autonomous co-pilot capabilities utilizing conversational LLMs for instant root-cause analysis and automated remediation."
            );
            default -> List.of(
                    "Expansion across adjacent domains and multi-tenant cloud environments to scale operational reach.",
                    "Integration with real-time IoT sensors and streaming data pipelines for automated continuous ingestion.",
                    "Deployment of predictive machine learning modules to forecast system anomalies 30–60 days in advance.",
                    "Development of native mobile applications providing real-time push alerts and emergency notifications."
            );
        };
    }

    private List<String> defaultReferences(String clean, Domain domain) {
        return switch (domain) {
            case BIOMEDICAL_HEALTHCARE -> List.of(
                    "[1] E. J. Topol, 'High-performance medicine: the convergence of human and artificial intelligence,' Nature Medicine, vol. 25, no. 1, pp. 44–56, 2019.",
                    "[2] A. Esteva et al., 'A guide to deep learning in healthcare,' Nature Medicine, vol. 25, pp. 24–29, 2019.",
                    "[3] R. R. Selvaraju et al., 'Grad-CAM: Visual Explanations from Deep Networks via Gradient-Based Localization,' IEEE International Conference on Computer Vision (ICCV), pp. 618-626, 2017.",
                    "[4] Nature Medicine, 'Clinical Validation and Deep Learning Deployments in Medical Imaging and Diagnostics,' vol. 30, 2024.",
                    "[5] World Health Organization, 'Ethics and Governance of Artificial Intelligence for Health,' WHO Technical Guidance, Geneva, 2023."
            );
            case AI_ENGINEERING -> List.of(
                    "[1] A. Vaswani et al., 'Attention Is All You Need,' Advances in Neural Information Processing Systems (NeurIPS), vol. 30, 2017.",
                    "[2] K. He, X. Zhang, S. Ren, and J. Sun, 'Deep Residual Learning for Image Recognition,' IEEE CVPR, pp. 770-778, 2016.",
                    "[3] M. Tan and Q. Le, 'EfficientNet: Rethinking Model Scaling for Convolutional Neural Networks,' ICML, pp. 6105-6114, 2019.",
                    "[4] IEEE Computer Society, 'Standardized Benchmarks for Real-Time Autonomous Inference Systems,' IEEE Transactions on Pattern Analysis and Machine Intelligence, 2024."
            );
            default -> List.of(
                    "[1] S. Russell and P. Norvig, Artificial Intelligence: A Modern Approach, 4th ed., Pearson, 2020.",
                    "[2] J. Dean and L. A. Barroso, 'The Tail at Scale,' Communications of the ACM, vol. 56, no. 2, pp. 74-80, 2013.",
                    "[3] IEEE Transactions on Engineering Management, 'Methodological Frameworks for Complex System Architecture and Evaluation,' vol. 71, 2024.",
                    "[4] ACM Computing Surveys, 'A Survey on Explainable Artificial Intelligence and Transparent Decision Systems,' ACM CSUR, 2023."
            );
        };
    }

    // ── Slide Selector & Narrative Sequencer ──────────────────────────────────────

    private List<SlideContent> selectAndRenumber(
            List<SlideBlueprint> masterSuite,
            int targetCount,
            String topic,
            String inst,
            String dept,
            List<String> team,
            String guide
    ) {
        // 1. Sort by priority
        List<SlideBlueprint> byPriority = new ArrayList<>(masterSuite);
        byPriority.sort(Comparator.comparingInt(SlideBlueprint::priority));

        // 2. Select top targetCount slides
        int countToPick = Math.min(targetCount, byPriority.size());
        List<SlideBlueprint> picked = new ArrayList<>(byPriority.subList(0, countToPick));

        // 3. Ensure Title Slide and Thank You Slide are always present
        boolean hasTitle = picked.stream().anyMatch(s -> "TITLE_SLIDE".equals(s.layoutType()));
        if (!hasTitle) {
            SlideBlueprint titleSlide = masterSuite.stream().filter(s -> "TITLE_SLIDE".equals(s.layoutType())).findFirst().orElse(null);
            if (titleSlide != null) {
                picked.remove(picked.size() - 1);
                picked.add(titleSlide);
            }
        }

        boolean hasThankYou = picked.stream().anyMatch(s -> "THANK_YOU".equals(s.layoutType()));
        if (!hasThankYou && targetCount >= 4) {
            SlideBlueprint thankYou = masterSuite.stream().filter(s -> "THANK_YOU".equals(s.layoutType())).findFirst().orElse(null);
            if (thankYou != null) {
                picked.remove(picked.size() - 1);
                picked.add(thankYou);
            }
        }

        // 4. Sort picked slides into the natural chronological order!
        picked.sort(Comparator.comparingInt(SlideBlueprint::narrativePosition));

        // 5. Convert to SlideContent
        String running = topic + (!inst.isBlank() ? " | " + inst : "");
        List<SlideContent> result = new ArrayList<>();

        for (int i = 0; i < picked.size(); i++) {
            SlideBlueprint bp = picked.get(i);
            int slideNum = i + 1;

            if ("TITLE_SLIDE".equals(bp.layoutType())) {
                result.add(SlideContent.titleSlide(bp.title(), bp.subtitle(), team, guide, inst, dept, slideNum));
            } else if ("THANK_YOU".equals(bp.layoutType())) {
                result.add(SlideContent.thankYouSlide(bp.title(), bp.subtitle(), running, slideNum));
            } else if ("AGENDA".equals(bp.layoutType())) {
                result.add(SlideContent.agendaSlide(bp.title(), bp.bullets(), running, slideNum));
            } else if ("ABSTRACT".equals(bp.layoutType())) {
                result.add(SlideContent.abstractSlide(bp.title(), bp.calloutDesc(), running, slideNum));
            } else if ("SCOPE".equals(bp.layoutType())) {
                result.add(SlideContent.scopeSlide(bp.title(), bp.bullets(), bp.rightColumn() != null ? List.of(bp.rightColumn().split("\n")) : List.of(), running, slideNum));
            } else if ("METHODOLOGY".equals(bp.layoutType())) {
                result.add(SlideContent.methodologySlide(bp.title(), bp.cardTitles(), bp.cardDescriptions(), running, slideNum));
            } else if ("TABLE".equals(bp.layoutType())) {
                result.add(SlideContent.tableSlide(bp.title(), bp.tableHeaders(), bp.tableRows(), bp.calloutNote(), running, bp.speakerNote(), slideNum));
            } else if ("REFERENCES".equals(bp.layoutType())) {
                result.add(SlideContent.referencesSlide(bp.title(), bp.bullets(), running, bp.speakerNote(), slideNum));
            } else {
                result.add(SlideContent.bulletsSlide(bp.title(), bp.bullets(), running, bp.speakerNote(), slideNum));
            }
        }

        return result;
    }
}
