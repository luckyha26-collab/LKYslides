/* ═══════════════════════════════════════════════════════════════════════════
   LKYslides — Academic & Project Presentation Engine
   Universal standard for student capstones, engineering defenses, and research decks.
═══════════════════════════════════════════════════════════════════════════ */

'use strict';

// ── Application State ────────────────────────────────────────────────────────
const state = {
  currentView: 'prompt',
  topic: '',
  content: '',
  slideCount: 14,
  tone: 'academic',
  format: 'engineering',
  institution: '',
  department: '',
  guide: '',
  teamMembers: [],
  themeId: 'DARK_CORPORATE',
  themeLabel: 'Obsidian Night',
  outline: [],
  slides: [],
  presentIndex: 0,
};

// ── Theme Registry ───────────────────────────────────────────────────────────
const THEMES = [
  {
    id: 'DARK_CORPORATE',
    bodyClass: 'theme-obsidian',
    label: 'Obsidian Night',
    desc: 'Sleek carbon dark with violet and electric cyan accents',
    bg: '#0f1117',
    accent: '#8b5cf6',
  },
  {
    id: 'ACADEMIC_BLUE',
    bodyClass: 'theme-academic',
    label: 'Academic Blue',
    desc: 'Prestige navy blue & warm gold — ideal for thesis & research defense',
    bg: '#081226',
    accent: '#f59e0b',
  },
  {
    id: 'MINIMAL_LIGHT',
    bodyClass: 'theme-paper',
    label: 'Clean Paper',
    desc: 'Ultra crisp ivory paper, graphite typography & indigo highlights',
    bg: '#ffffff',
    accent: '#6366f1',
  },
  {
    id: 'GRADIENT_MODERN',
    bodyClass: 'theme-sunset',
    label: 'Neon Sunset',
    desc: 'Deep twilight purple blending with warm coral and amber',
    bg: '#0f0717',
    accent: '#f43f5e',
  },
  {
    id: 'EARTHY_WARM',
    bodyClass: 'theme-earthy',
    label: 'Earthy Clay',
    desc: 'Warm terracotta, sand cream, and editorial serif styling',
    bg: '#fcfaf7',
    accent: '#c25e2e',
  },
  {
    id: 'VIBRANT_CREATIVE',
    bodyClass: 'theme-cyber',
    label: 'Cyber Mint',
    desc: 'Pitch obsidian with vivid electric mint & high contrast',
    bg: '#060a08',
    accent: '#10b981',
  },
];

const $ = (id) => document.getElementById(id);
const $$ = (sel) => document.querySelectorAll(sel);

// ═══════════════════════════════════════════════════════════════════════════
//  INITIALIZATION
// ═══════════════════════════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', () => {
  setupPromptListeners();
  setupFormatChips();
  setupOptionPills();
  renderThemeModal();
  setupKeyboardNavigation();
  updateThemeUI('DARK_CORPORATE');
});

function switchView(viewName) {
  state.currentView = viewName;
  $('viewPrompt').hidden = viewName !== 'prompt';
  $('viewOutline').hidden = viewName !== 'outline';
  $('viewDeck').hidden = viewName !== 'deck';

  $('tabPrompt').classList.toggle('active', viewName === 'prompt');
  $('tabOutline').classList.toggle('active', viewName === 'outline');
  $('tabDeck').classList.toggle('active', viewName === 'deck');

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ═══════════════════════════════════════════════════════════════════════════
//  VIEW 1: PROMPT SETUP & PRE-FILL
// ═══════════════════════════════════════════════════════════════════════════
function setupPromptListeners() {
  const promptInput = $('promptTopic');
  promptInput.addEventListener('input', () => {
    const len = promptInput.value.trim().length;
    $('promptCharCount').textContent = `${len} character${len !== 1 ? 's' : ''}`;

    const firstLine = promptInput.value.trim().split('\n')[0];
    $('headerDeckTitle').textContent = firstLine ? firstLine.slice(0, 45) : 'Untitled Presentation';
  });

  $('headerDeckTitle').addEventListener('input', (e) => {
    state.topic = e.target.textContent.trim();
  });
}

function fillPrompt(text) {
  const promptInput = $('promptTopic');
  promptInput.value = text;
  promptInput.dispatchEvent(new Event('input'));
  promptInput.focus();
}

function setupFormatChips() {
  $$('.format-chip').forEach((chip) => {
    chip.addEventListener('click', () => {
      $$('.format-chip').forEach((c) => c.classList.remove('active'));
      chip.classList.add('active');
      state.format = chip.dataset.format;
    });
  });
}

function setupOptionPills() {
  $$('.card-num-pill').forEach((pill) => {
    pill.addEventListener('click', () => {
      $$('.card-num-pill').forEach((p) => p.classList.remove('active'));
      pill.classList.add('active');
      state.slideCount = parseInt(pill.dataset.count, 10);
    });
  });

  $$('.tone-pill').forEach((pill) => {
    pill.addEventListener('click', () => {
      $$('.tone-pills .tone-pill').forEach((p) => p.classList.remove('active'));
      pill.classList.add('active');
      state.tone = pill.dataset.tone;
    });
  });
}

function toggleAdvancedOptions() {
  const panel = $('advancedSettingsPanel');
  panel.hidden = !panel.hidden;
  $('optToggleText').textContent = panel.hidden ? 'College & Team Credentials' : 'Hide Credentials';
}

function collectPresentationRequest() {
  const rawText = $('promptTopic').value.trim();
  if (!rawText) {
    showToast('Please enter a presentation topic or project title.', '⚠️');
    $('promptTopic').focus();
    return null;
  }

  const lines = rawText.split('\n').filter((l) => l.trim().length > 0);
  state.topic = lines[0].slice(0, 180);
  state.content = rawText;
  state.institution = $('inputInstitution')?.value.trim() || '';
  state.department = $('inputDepartment')?.value.trim() || '';
  state.guide = $('inputGuide')?.value.trim() || '';

  const rawMembers = $('inputTeamMembers')?.value.trim() || '';
  state.teamMembers = rawMembers
    ? rawMembers.split(',').map((s) => s.trim()).filter((s) => s.length > 0)
    : [];

  return {
    topic: state.topic,
    content: state.content,
    slideCount: state.slideCount,
    theme: state.themeId,
    tone: state.tone,
    institution: state.institution,
    department: state.department,
    projectGuide: state.guide,
    teamMembers: state.teamMembers,
    presentedBy: state.teamMembers.join(', '),
  };
}

// ═══════════════════════════════════════════════════════════════════════════
//  VIEW 2: OUTLINE STUDIO
// ═══════════════════════════════════════════════════════════════════════════
async function startGenerateOutline() {
  const req = collectPresentationRequest();
  if (!req) return;

  showLoading('Synthesizing academic outline…', 'Structuring research matrices and algorithm workflows');

  try {
    const res = await fetch('/api/outline', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Server error while generating outline');
    }

    state.outline = await res.json();
    hideLoading();

    renderOutlineStudio();
    $('tabOutline').disabled = false;
    switchView('outline');
    showToast(`Generated ${state.outline.length}-section outline. You can customize before finalizing.`, '✦');

  } catch (err) {
    hideLoading();
    showToast(err.message || 'Failed to generate outline', '⚠️');
  }
}

function renderOutlineStudio() {
  const stack = $('outlineCardStack');
  stack.innerHTML = '';

  $('outlineStatsPill').textContent = `${state.outline.length} Cards Planned`;

  state.outline.forEach((card, idx) => {
    const cardEl = document.createElement('div');
    cardEl.className = 'outline-item-card';
    cardEl.dataset.index = idx;

    cardEl.innerHTML = `
      <div class="outline-item-top">
        <span class="outline-card-index">${String(idx + 1).padStart(2, '0')}</span>
        <span class="outline-layout-badge">${formatLayoutName(card.layoutType)}</span>
        <div class="outline-tools">
          <button type="button" class="btn-card-ctrl" title="Move Up" onclick="moveOutlineCard(${idx}, -1)">↑</button>
          <button type="button" class="btn-card-ctrl" title="Move Down" onclick="moveOutlineCard(${idx}, 1)">↓</button>
          <button type="button" class="btn-card-ctrl danger" title="Delete Card" onclick="deleteOutlineCard(${idx})">🗑️</button>
        </div>
      </div>
      <input
        type="text"
        class="outline-title-input"
        value="${escapeHtml(card.title)}"
        oninput="updateOutlineTitle(${idx}, this.value)"
      />
      <div class="outline-summary">${escapeHtml(card.summary || '')}</div>
      <div class="outline-keypoints-list">
        ${(card.keyPoints || []).map((kp) => `<span class="outline-point-tag">${escapeHtml(kp)}</span>`).join('')}
      </div>
    `;

    stack.appendChild(cardEl);
  });
}

function formatLayoutName(type) {
  switch (type) {
    case 'TITLE_SLIDE':    return '📑 Institutional Title';
    case 'SPLIT_CALLOUT':  return '⚡ Split Metric Callout';
    case 'GRID_CARDS':     return '🔲 Numbered Grid Cards';
    case 'TABLE':          return '📊 Technical Matrix Table';
    case 'ALGORITHM_FLOW': return '⚙️ Mathematical Model Flow';
    case 'METRICS_DETAIL': return '📈 KPI Outcome Benchmark';
    case 'REFERENCES':     return '📚 Academic Bibliography';
    case 'THANK_YOU':      return '🎯 Closing & Q&A';
    default:               return '📝 Key Points';
  }
}

function updateOutlineTitle(index, newTitle) {
  if (state.outline[index]) {
    state.outline[index].title = newTitle;
  }
}

function moveOutlineCard(index, direction) {
  const target = index + direction;
  if (target < 0 || target >= state.outline.length) return;
  const temp = state.outline[index];
  state.outline[index] = state.outline[target];
  state.outline[target] = temp;
  renderOutlineStudio();
}

function deleteOutlineCard(index) {
  if (state.outline.length <= 3) {
    showToast('A presentation needs at least 3 cards.', 'ℹ️');
    return;
  }
  state.outline.splice(index, 1);
  renderOutlineStudio();
}

function addNewOutlineCard() {
  const newNum = state.outline.length + 1;
  state.outline.push({
    index: newNum,
    title: `Key Technical Analysis: ${state.topic}`,
    layoutType: 'GRID_CARDS',
    summary: 'Detailed evaluation matrix and performance metrics.',
    keyPoints: ['Feature 1: Rigorous evaluation', 'Feature 2: Scalable architecture'],
  });
  renderOutlineStudio();
}

// ═══════════════════════════════════════════════════════════════════════════
//  VIEW 3: LIVE GAMMA DECK STUDIO
// ═══════════════════════════════════════════════════════════════════════════
async function generateCardsFromOutline() {
  const req = {
    topic: state.topic,
    content: state.content,
    slideCount: state.outline.length,
    theme: state.themeId,
    tone: state.tone,
    institution: state.institution,
    department: state.department,
    projectGuide: state.guide,
    teamMembers: state.teamMembers,
    presentedBy: state.teamMembers.join(', '),
  };

  showLoading('Generating presentation cards…', 'Compiling data tables, algorithm models, and metric cards');

  try {
    const res = await fetch('/api/generate-slides', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });

    if (!res.ok) throw new Error('Failed to generate presentation slides');

    let slides = await res.json();

    if (state.outline.length > 0) {
      slides = slides.map((s, idx) => {
        if (state.outline[idx] && state.outline[idx].title) {
          s.title = state.outline[idx].title;
        }
        return s;
      });
    }

    state.slides = slides;
    hideLoading();

    renderLiveDeckStudio();
    $('tabDeck').disabled = false;
    $('presentBtn').disabled = false;
    switchView('deck');
    showToast('Presentation ready! Click any text or cell to edit live.', '🎉');

  } catch (err) {
    hideLoading();
    showToast(err.message || 'Generation error', '⚠️');
  }
}

// ── Render Full Live Deck ────────────────────────────────────────────────────
function renderLiveDeckStudio() {
  const container = $('cardsCanvasStream');
  container.innerHTML = '';

  $('deckCardsBadge').textContent = `${state.slides.length} CARDS`;

  state.slides.forEach((slide, idx) => {
    const cardEl = document.createElement('article');
    cardEl.className = 'gamma-live-card';
    cardEl.dataset.slideIndex = idx;

    // Header bar
    const headerHtml = `
      <div class="live-card-header">
        <div class="card-badge-pill">
          <span class="card-num">${String(idx + 1).padStart(2, '0')}</span>
          <span>/</span>
          <span>${String(state.slides.length).padStart(2, '0')}</span>
          <span>•</span>
          <span>${formatLayoutName(slide.layoutType)}</span>
        </div>
        <div class="live-card-actions">
          <button type="button" class="btn-card-ctrl danger" title="Remove Card" onclick="deleteLiveCard(${idx})">🗑️</button>
        </div>
      </div>
    `;

    // Dynamic Body based on layout
    let bodyHtml = '';
    switch (slide.layoutType) {
      case 'TITLE_SLIDE':
        bodyHtml = renderTitleSlideHtml(slide, idx);
        break;
      case 'AGENDA':
        bodyHtml = renderAgendaHtml(slide, idx);
        break;
      case 'ABSTRACT':
        bodyHtml = renderAbstractHtml(slide, idx);
        break;
      case 'SCOPE':
        bodyHtml = renderScopeHtml(slide, idx);
        break;
      case 'METHODOLOGY':
        bodyHtml = renderMethodologyHtml(slide, idx);
        break;
      case 'BULLETS':
        bodyHtml = renderStandardContentHtml(slide, idx);
        break;
      case 'SPLIT_CALLOUT':
        bodyHtml = renderSplitCalloutHtml(slide, idx);
        break;
      case 'GRID_CARDS':
        bodyHtml = renderGridCardsHtml(slide, idx);
        break;
      case 'TABLE':
        bodyHtml = renderTableSlideHtml(slide, idx);
        break;
      case 'ALGORITHM_FLOW':
        bodyHtml = renderAlgorithmFlowHtml(slide, idx);
        break;
      case 'METRICS_DETAIL':
        bodyHtml = renderMetricsDetailHtml(slide, idx);
        break;
      case 'TWO_COLUMN':
        bodyHtml = renderTwoColumnHtml(slide, idx);
        break;
      case 'REFERENCES':
        bodyHtml = renderReferencesHtml(slide, idx);
        break;
      case 'THANK_YOU':
        bodyHtml = renderThankYouHtml(slide, idx);
        break;
      default:
        bodyHtml = renderStandardContentHtml(slide, idx);
        break;
    }

    // Running institutional footer
    const runningText = slide.runningHeader || `${state.topic} | ${state.institution}`;
    const footerHtml = slide.layoutType !== 'THANK_YOU' ? `
      <div class="card-running-footer">
        <span>${escapeHtml(runningText)}</span>
        <span>${idx + 1}</span>
      </div>
    ` : '';

    // Collapsible Speaker Notes
    const notesHtml = `
      <div class="speaker-notes-drawer">
        <button type="button" class="notes-toggle-btn" onclick="toggleSpeakerNote(${idx})">
          🎙️ Speaker Notes ▾
        </button>
        <div class="notes-content-box" id="noteBox_${idx}" contenteditable="true" spellcheck="false" onblur="updateLiveSlideNote(${idx}, this.innerText)">
          ${escapeHtml(slide.speakerNote || 'Deliver a structured walkthrough highlighting key technical takeaways.')}
        </div>
      </div>
    `;

    cardEl.innerHTML = headerHtml + bodyHtml + footerHtml + notesHtml;
    container.appendChild(cardEl);
  });
}

// ── Dynamic Layout Renderers (High-Density Academic & Engineering Layouts) ──

// 1. Title Slide (Slide 1)
function renderTitleSlideHtml(s, idx) {
  const members = s.teamMembers && s.teamMembers.length > 0 ? s.teamMembers : (state.teamMembers || []);
  const guide = s.projectGuide || state.guide || '';
  const inst = s.institution || state.institution || '';
  const dept = s.department || state.department || '';

  return `
    ${(inst || dept) ? `
    <div class="title-college-banner">
      ${inst ? `<div class="college-name">${escapeHtml(inst)}</div>` : ''}
      ${dept ? `<div class="college-dept">${escapeHtml(dept)}</div>` : ''}
    </div>` : ''}

    <div class="title-center-card">
      <h1 class="card-h1" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || state.topic || 'Engineering Presentation')}
      </h1>
      ${(s.subtitle || s.tagline) ? `
      <div class="title-sub-text" contenteditable="true" spellcheck="false" onblur="updateLiveSlideSubtitle(${idx}, this.innerText)">
        ${escapeHtml(s.subtitle || s.tagline || '')}
      </div>` : ''}
    </div>

    ${(members.length > 0 || guide) ? `
    <div class="title-roles-row">
      ${members.length > 0 ? `
      <div class="role-block">
        <div class="role-title">Project Team</div>
        <div class="role-names-list">
          ${members.map((m) => `<div>${escapeHtml(m)}</div>`).join('')}
        </div>
      </div>` : '<div></div>'}
      ${guide ? `
      <div class="role-block" style="text-align: right;">
        <div class="role-title">Project Guide / Supervisor</div>
        <div class="role-names-list">
          <div><strong>${escapeHtml(guide)}</strong></div>
          ${dept ? `<div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(dept)}</div>` : ''}
        </div>
      </div>` : ''}
    </div>` : ''}
  `;
}

// 2. Split Callout Slide (Slide 2 & 3 in PDF)
function renderSplitCalloutHtml(s, idx) {
  const bullets = s.bullets || [];
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title)}
      </h2>
      <div class="split-callout-grid">
        <div class="callout-bullets-col">
          ${bullets.map((b, bIdx) => `
            <div class="callout-bullet-row">
              <span class="bullet-dot-bullet">•</span>
              <div class="bullet-text-main" contenteditable="true" spellcheck="false" onblur="updateLiveSlideBullet(${idx}, ${bIdx}, this.innerText)">
                ${escapeHtml(b)}
              </div>
            </div>
          `).join('')}
        </div>
        <div class="dark-callout-card">
          <div class="callout-big-metric" contenteditable="true" spellcheck="false" onblur="updateLiveSlideCalloutMetric(${idx}, this.innerText)">
            ${escapeHtml(s.calloutMetric || '99.4%')}
          </div>
          <div class="callout-sub-label" contenteditable="true" spellcheck="false" onblur="updateLiveSlideCalloutLabel(${idx}, this.innerText)">
            ${escapeHtml(s.calloutLabel || 'Key Benchmark')}
          </div>
          <div class="callout-desc-box" contenteditable="true" spellcheck="false" onblur="updateLiveSlideCalloutDesc(${idx}, this.innerText)">
            ${escapeHtml(s.calloutDescription || '')}
          </div>
        </div>
      </div>
    </div>
  `;
}

// 3. Numbered Grid Cards (Slide 4, 11, 12 in PDF)
function renderGridCardsHtml(s, idx) {
  const titles = s.cardTitles || ['Objective 1', 'Objective 2', 'Objective 3', 'Objective 4'];
  const descs = s.cardDescriptions || ['Description 1', 'Description 2', 'Description 3', 'Description 4'];

  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title)}
      </h2>
      <div class="grid-cards-container">
        ${titles.map((title, cIdx) => `
          <div class="numbered-grid-card">
            <div class="card-num-circle">${cIdx + 1}</div>
            <div class="card-body-wrap">
              <div class="card-item-title" contenteditable="true" spellcheck="false" onblur="updateLiveSlideGridTitle(${idx}, ${cIdx}, this.innerText)">
                ${escapeHtml(title)}
              </div>
              <div class="card-item-desc" contenteditable="true" spellcheck="false" onblur="updateLiveSlideGridDesc(${idx}, ${cIdx}, this.innerText)">
                ${escapeHtml(descs[cIdx] || '')}
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// 4. Table Slide (Slide 5, 6, 7, 10 in PDF)
function renderTableSlideHtml(s, idx) {
  const headers = s.tableHeaders || ['Col 1', 'Col 2', 'Col 3'];
  const rows = s.tableRows || [];

  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title)}
      </h2>
      <div class="table-responsive-container">
        <table class="academic-data-table">
          <thead>
            <tr>
              ${headers.map((h) => `<th>${escapeHtml(h)}</th>`).join('')}
            </tr>
          </thead>
          <tbody>
            ${rows.map((row, rIdx) => `
              <tr>
                ${row.map((cell, cIdx) => `
                  <td contenteditable="true" spellcheck="false" onblur="updateLiveSlideTableCell(${idx}, ${rIdx}, ${cIdx}, this.innerText)">
                    ${escapeHtml(cell)}
                  </td>
                `).join('')}
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
      ${s.calloutNote ? `
        <div class="table-highlight-banner" contenteditable="true" spellcheck="false" onblur="updateLiveSlideCalloutNote(${idx}, this.innerText)">
          ${escapeHtml(s.calloutNote)}
        </div>
      ` : ''}
    </div>
  `;
}

// 5. Algorithm Flow Slide (Slide 9 in PDF)
function renderAlgorithmFlowHtml(s, idx) {
  const inputs = s.algorithmInputs || ['Input Vector', 'Parameter Matrix', 'State Variables', 'Sensor Stream'];
  const tiers = s.algorithmTiers || ['Tier 1: Nominal', 'Tier 2: Monitored', 'Tier 3: Elevated Risk', 'Tier 4: Critical'];
  const tierClasses = ['tier-healthy', 'tier-slow-moving', 'tier-high-risk', 'tier-dead-stock'];

  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Core Algorithm & Execution Pipeline')}
      </h2>
      <div class="algo-flow-container">
        <div class="algo-inputs-box">
          <div class="algo-box-heading">Input Parameters</div>
          <div class="algo-inputs-list">
            ${inputs.map((inp) => `<div class="algo-input-item">• ${escapeHtml(inp)}</div>`).join('')}
          </div>
        </div>

        <div class="algo-flow-arrow">➔</div>

        <div class="algo-core-card">
          <div class="core-engine-title">${escapeHtml(s.algorithmCoreName || 'CORE INFERENCE ENGINE')}</div>
          <div class="core-engine-desc">${escapeHtml(s.algorithmCoreDesc || 'Algorithmic decision pipeline\nHigher score = critical priority')}</div>
        </div>

        <div class="algo-flow-arrow">➔</div>

        <div class="algo-tiers-box">
          <div class="algo-box-heading">Classification / Risk Tiers</div>
          <div class="algo-tiers-list">
            ${tiers.map((tier, tIdx) => `
              <div class="tier-pill ${tierClasses[tIdx % tierClasses.length]}">
                ${escapeHtml(tier)}
              </div>
            `).join('')}
          </div>
        </div>
      </div>
      ${s.calloutNote ? `
        <div class="table-highlight-banner" style="font-size: 0.8rem; font-style: italic;">
          ${escapeHtml(s.calloutNote)}
        </div>
      ` : ''}
    </div>
  `;
}

// 6. Metrics Detail Slide (Slide 10 in PDF)
function renderMetricsDetailHtml(s, idx) {
  const values = s.statValues || ['98.4%', '< 12ms', '4.8x', '99.9%'];
  const labels = s.statLabels || ['System Accuracy', 'Inference Latency', 'Throughput Speedup', 'Uptime Reliability'];
  const bullets = s.bullets || [];

  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Expected Outcomes & Performance Benchmarks')}
      </h2>
      <div class="metrics-row-top">
        ${values.map((v, vIdx) => `
          <div class="kpi-tile-card">
            <div class="kpi-tile-number" contenteditable="true" spellcheck="false" onblur="updateLiveSlideMetricVal(${idx}, ${vIdx}, this.innerText)">
              ${escapeHtml(v)}
            </div>
            <div class="kpi-tile-label" contenteditable="true" spellcheck="false" onblur="updateLiveSlideMetricLbl(${idx}, ${vIdx}, this.innerText)">
              ${escapeHtml(labels[vIdx] || '')}
            </div>
          </div>
        `).join('')}
      </div>
      <div class="callout-bullets-col">
        ${bullets.map((b, bIdx) => `
          <div class="callout-bullet-row">
            <span class="bullet-dot-bullet">•</span>
            <div class="bullet-text-main" contenteditable="true" spellcheck="false" onblur="updateLiveSlideBullet(${idx}, ${bIdx}, this.innerText)">
              ${escapeHtml(b)}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// 7. References Slide (Slide 14 in PDF)
function renderReferencesHtml(s, idx) {
  const citations = s.bullets || [];
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'References')}
      </h2>
      <div class="references-list-card">
        ${citations.map((c, cIdx) => `
          <div class="reference-item-p" contenteditable="true" spellcheck="false" onblur="updateLiveSlideBullet(${idx}, ${cIdx}, this.innerText)">
            ${escapeHtml(c)}
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// 8. Thank You Slide (Slide 15 in PDF)
function renderThankYouHtml(s, idx) {
  const sub = s.subtitle || (state.topic ? `Questions & Discussion | ${state.topic}` : 'Questions & Discussion');
  return `
    <div class="thank-you-hero">
      <h1 class="thank-you-title" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        Thank You
      </h1>
      <div class="thank-you-sub" contenteditable="true" spellcheck="false" onblur="updateLiveSlideSubtitle(${idx}, this.innerText)">
        ${escapeHtml(sub)}
      </div>
    </div>
  `;
}

// 9. Agenda / Content Slide
function renderAgendaHtml(s, idx) {
  const items = s.bullets || s.cardTitles || [];
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Content')}
      </h2>
      <div class="agenda-items-grid">
        ${items.map((it, itIdx) => `
          <div class="agenda-item-card">
            <span class="agenda-num">${String(itIdx + 1).padStart(2, '0')}.</span>
            <span class="agenda-text" contenteditable="true" spellcheck="false" onblur="updateLiveSlideBullet(${idx}, ${itIdx}, this.innerText)">
              ${escapeHtml(it)}
            </span>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// 10. Abstract Slide
function renderAbstractHtml(s, idx) {
  const desc = s.calloutDescription || (s.bullets ? s.bullets.join('\n\n') : '');
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Abstract')}
      </h2>
      <div class="abstract-hero-box" contenteditable="true" spellcheck="false" onblur="updateLiveSlideCalloutDesc(${idx}, this.innerText)">
        ${escapeHtml(desc)}
      </div>
    </div>
  `;
}

// 11. Scope Slide (In-Scope vs Out-of-Scope)
function renderScopeHtml(s, idx) {
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Scope')}
      </h2>
      <div class="scope-split-grid">
        <div class="scope-box in-scope">
          <div class="scope-header-title">✔ IN SCOPE</div>
          <div class="scope-content-body" contenteditable="true" spellcheck="false" onblur="updateLiveSlideLeftCol(${idx}, this.innerText)">
            ${escapeHtml(s.leftColumn || '')}
          </div>
        </div>
        <div class="scope-box out-scope">
          <div class="scope-header-title">✖ OUT OF SCOPE</div>
          <div class="scope-content-body" contenteditable="true" spellcheck="false" onblur="updateLiveSlideRightCol(${idx}, this.innerText)">
            ${escapeHtml(s.rightColumn || '')}
          </div>
        </div>
      </div>
    </div>
  `;
}

// 12. Methodology Slide
function renderMethodologyHtml(s, idx) {
  const titles = s.cardTitles || [];
  const descs = s.cardDescriptions || [];
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title || 'Proposed Methodology')}
      </h2>
      <div class="methodology-phases-grid">
        ${titles.map((t, pIdx) => `
          <div class="methodology-phase-card">
            <div class="phase-badge">Phase ${pIdx + 1}</div>
            <div class="phase-title" contenteditable="true" spellcheck="false" onblur="updateLiveSlideGridTitle(${idx}, ${pIdx}, this.innerText)">
              ${escapeHtml(t)}
            </div>
            <div class="phase-desc" contenteditable="true" spellcheck="false" onblur="updateLiveSlideGridDesc(${idx}, ${pIdx}, this.innerText)">
              ${escapeHtml(descs[pIdx] || '')}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// Standard Content Fallback
function renderStandardContentHtml(s, idx) {
  const bullets = s.bullets || ['Detailed technical insight.'];
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title)}
      </h2>
      <div class="callout-bullets-col">
        ${bullets.map((b, bIdx) => `
          <div class="callout-bullet-row">
            <span class="bullet-dot-bullet">•</span>
            <div class="bullet-text-main" contenteditable="true" spellcheck="false" onblur="updateLiveSlideBullet(${idx}, ${bIdx}, this.innerText)">
              ${escapeHtml(b)}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// 9. Two Column Slide
function renderTwoColumnHtml(s, idx) {
  return `
    <div class="card-content-body">
      <h2 class="card-h2" contenteditable="true" spellcheck="false" onblur="updateLiveSlideTitle(${idx}, this.innerText)">
        ${escapeHtml(s.title)}
      </h2>
      <div class="two-column-grid">
        <div class="two-column-card-box" contenteditable="true" spellcheck="false" onblur="updateLiveSlideLeftCol(${idx}, this.innerText)">
          ${escapeHtml(s.leftColumn || '')}
        </div>
        <div class="two-column-card-box highlight" contenteditable="true" spellcheck="false" onblur="updateLiveSlideRightCol(${idx}, this.innerText)">
          ${escapeHtml(s.rightColumn || '')}
        </div>
      </div>
    </div>
  `;
}

// ── Live Editing Callbacks ───────────────────────────────────────────────────
function updateLiveSlideTitle(idx, val) { if (state.slides[idx]) state.slides[idx].title = val.trim(); }
function updateLiveSlideSubtitle(idx, val) { if (state.slides[idx]) state.slides[idx].subtitle = val.trim(); }
function updateLiveSlideLeftCol(idx, val) { if (state.slides[idx]) state.slides[idx].leftColumn = val.trim(); }
function updateLiveSlideRightCol(idx, val) { if (state.slides[idx]) state.slides[idx].rightColumn = val.trim(); }
function updateLiveSlideBullet(sIdx, bIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].bullets) state.slides[sIdx].bullets[bIdx] = val.trim();
}
function updateLiveSlideCalloutMetric(idx, val) { if (state.slides[idx]) state.slides[idx].calloutMetric = val.trim(); }
function updateLiveSlideCalloutLabel(idx, val) { if (state.slides[idx]) state.slides[idx].calloutLabel = val.trim(); }
function updateLiveSlideCalloutDesc(idx, val) { if (state.slides[idx]) state.slides[idx].calloutDescription = val.trim(); }
function updateLiveSlideGridTitle(sIdx, cIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].cardTitles) state.slides[sIdx].cardTitles[cIdx] = val.trim();
}
function updateLiveSlideGridDesc(sIdx, cIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].cardDescriptions) state.slides[sIdx].cardDescriptions[cIdx] = val.trim();
}
function updateLiveSlideTableCell(sIdx, rIdx, cIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].tableRows && state.slides[sIdx].tableRows[rIdx]) {
    state.slides[sIdx].tableRows[rIdx][cIdx] = val.trim();
  }
}
function updateLiveSlideCalloutNote(idx, val) { if (state.slides[idx]) state.slides[idx].calloutNote = val.trim(); }
function updateLiveSlideMetricVal(sIdx, vIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].statValues) state.slides[sIdx].statValues[vIdx] = val.trim();
}
function updateLiveSlideMetricLbl(sIdx, vIdx, val) {
  if (state.slides[sIdx] && state.slides[sIdx].statLabels) state.slides[sIdx].statLabels[vIdx] = val.trim();
}
function updateLiveSlideNote(idx, val) { if (state.slides[idx]) state.slides[idx].speakerNote = val.trim(); }

function toggleSpeakerNote(idx) {
  const box = $(`noteBox_${idx}`);
  if (box) box.style.display = box.style.display === 'none' ? 'block' : 'none';
}

function deleteLiveCard(idx) {
  if (state.slides.length <= 2) {
    showToast('Deck must retain at least 2 cards.', 'ℹ️');
    return;
  }
  state.slides.splice(idx, 1);
  renderLiveDeckStudio();
}

function addNewSlideToDeck() {
  const nextNum = state.slides.length + 1;
  state.slides.push({
    layoutType: 'GRID_CARDS',
    title: `Key Technical Insights: ${state.topic}`,
    cardTitles: ['Core Insight 1', 'Core Insight 2', 'Core Insight 3', 'Core Insight 4'],
    cardDescriptions: ['Detailed operational verification.', 'Quantified improvement metric.', 'Risk threshold analysis.', 'Empirical test results.'],
    speakerNote: 'Walk evaluators through these additional verification points.',
    slideNumber: nextNum,
    runningHeader: state.institution ? `${state.topic || 'Project'} | ${state.institution}` : (state.topic || 'Engineering Presentation'),
  });
  renderLiveDeckStudio();
  window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
}

// ═══════════════════════════════════════════════════════════════════════════
//  THEME STUDIO MODAL
// ═══════════════════════════════════════════════════════════════════════════
function renderThemeModal() {
  const grid = $('themesSelectorGrid');
  grid.innerHTML = '';

  THEMES.forEach((t) => {
    const card = document.createElement('div');
    card.className = `theme-swatch-card ${t.id === state.themeId ? 'active' : ''}`;
    card.dataset.themeId = t.id;

    card.innerHTML = `
      <div class="swatch-color-preview" style="background:${t.bg}">
        <div class="swatch-pill" style="background:${t.accent}"></div>
        <div class="swatch-line" style="background:${t.accent}"></div>
      </div>
      <div class="theme-swatch-name">${escapeHtml(t.label)}</div>
      <div class="theme-swatch-desc">${escapeHtml(t.desc)}</div>
    `;

    card.addEventListener('click', () => {
      updateThemeUI(t.id);
      toggleThemeModal();
      showToast(`Applied ${t.label} theme.`, '🎨');
    });

    grid.appendChild(card);
  });
}

function toggleThemeModal() {
  const modal = $('themeModalOverlay');
  modal.hidden = !modal.hidden;
}

function closeThemeModal() {
  $('themeModalOverlay').hidden = true;
}

function updateThemeUI(themeId) {
  const found = THEMES.find((t) => t.id === themeId) || THEMES[0];
  state.themeId = found.id;
  state.themeLabel = found.label;

  THEMES.forEach((t) => document.body.classList.remove(t.bodyClass));
  document.body.classList.add(found.bodyClass);

  $('activeThemeDot').style.background = found.accent;
  $('activeThemeLabel').textContent = found.label;

  $$('.theme-swatch-card').forEach((c) => {
    c.classList.toggle('active', c.dataset.themeId === found.id);
  });
}

// ═══════════════════════════════════════════════════════════════════════════
//  FULLSCREEN PRESENTATION MODE
// ═══════════════════════════════════════════════════════════════════════════
function startPresentationMode() {
  if (!state.slides || state.slides.length === 0) return;
  state.presentIndex = 0;
  $('presentOverlay').hidden = false;
  $('presentTitle').textContent = state.topic || 'Presentation';
  renderPresentSlide();
}

function exitPresentationMode() {
  $('presentOverlay').hidden = true;
}

function renderPresentSlide() {
  const slide = state.slides[state.presentIndex];
  if (!slide) return;

  $('presentCounter').textContent = `Card ${state.presentIndex + 1} / ${state.slides.length}`;

  const container = $('presentCardContainer');
  let bodyHtml = '';
  switch (slide.layoutType) {
    case 'TITLE_SLIDE':
      bodyHtml = renderTitleSlideHtml(slide, state.presentIndex);
      break;
    case 'AGENDA':
      bodyHtml = renderAgendaHtml(slide, state.presentIndex);
      break;
    case 'ABSTRACT':
      bodyHtml = renderAbstractHtml(slide, state.presentIndex);
      break;
    case 'SCOPE':
      bodyHtml = renderScopeHtml(slide, state.presentIndex);
      break;
    case 'METHODOLOGY':
      bodyHtml = renderMethodologyHtml(slide, state.presentIndex);
      break;
    case 'BULLETS':
      bodyHtml = renderStandardContentHtml(slide, state.presentIndex);
      break;
    case 'SPLIT_CALLOUT':
      bodyHtml = renderSplitCalloutHtml(slide, state.presentIndex);
      break;
    case 'GRID_CARDS':
      bodyHtml = renderGridCardsHtml(slide, state.presentIndex);
      break;
    case 'TABLE':
      bodyHtml = renderTableSlideHtml(slide, state.presentIndex);
      break;
    case 'ALGORITHM_FLOW':
      bodyHtml = renderAlgorithmFlowHtml(slide, state.presentIndex);
      break;
    case 'METRICS_DETAIL':
      bodyHtml = renderMetricsDetailHtml(slide, state.presentIndex);
      break;
    case 'TWO_COLUMN':
      bodyHtml = renderTwoColumnHtml(slide, state.presentIndex);
      break;
    case 'REFERENCES':
      bodyHtml = renderReferencesHtml(slide, state.presentIndex);
      break;
    case 'THANK_YOU':
      bodyHtml = renderThankYouHtml(slide, state.presentIndex);
      break;
    default:
      bodyHtml = renderStandardContentHtml(slide, state.presentIndex);
      break;
  }

  container.innerHTML = `
    <div class="gamma-live-card" style="width:100%; max-height:85vh; overflow-y:auto;">
      ${bodyHtml}
    </div>
  `;

  const dotsContainer = $('presentDots');
  dotsContainer.innerHTML = '';
  state.slides.forEach((_, idx) => {
    const dot = document.createElement('div');
    dot.className = `p-dot ${idx === state.presentIndex ? 'active' : ''}`;
    dot.onclick = () => {
      state.presentIndex = idx;
      renderPresentSlide();
    };
    dotsContainer.appendChild(dot);
  });
}

function nextPresentSlide() {
  if (state.presentIndex < state.slides.length - 1) {
    state.presentIndex++;
    renderPresentSlide();
  }
}

function prevPresentSlide() {
  if (state.presentIndex > 0) {
    state.presentIndex--;
    renderPresentSlide();
  }
}

function setupKeyboardNavigation() {
  window.addEventListener('keydown', (e) => {
    if (!$('presentOverlay').hidden) {
      if (e.key === 'ArrowRight' || e.key === ' ') {
        e.preventDefault();
        nextPresentSlide();
      } else if (e.key === 'ArrowLeft') {
        e.preventDefault();
        prevPresentSlide();
      } else if (e.key === 'Escape') {
        exitPresentationMode();
      }
    }
  });
}

// ═══════════════════════════════════════════════════════════════════════════
//  EXPORT TO NATIVE POWERPOINT (.PPTX)
// ═══════════════════════════════════════════════════════════════════════════
async function exportCurrentDeckPptx() {
  if (!state.slides || state.slides.length === 0) {
    startDirectPptxDownload();
    return;
  }

  showLoading('Exporting presentation…', 'Compiling watermark-free .pptx file via Apache POI');

  try {
    const payload = {
      topic: state.topic || 'presentation',
      theme: state.themeId,
      slides: state.slides,
    };

    const res = await fetch('/api/export-pptx', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) throw new Error('PPTX export failed on server');

    const disposition = res.headers.get('Content-Disposition') || '';
    const match = disposition.match(/filename="?([^"]+)"?/);
    const filename = match ? match[1] : `${sanitizeFilename(state.topic)}_lkyslides.pptx`;

    const blob = await res.blob();
    triggerFileDownload(blob, filename);

    hideLoading();
    showToast('PowerPoint presentation downloaded! 100% watermark-free.', '🎉');

  } catch (err) {
    hideLoading();
    showToast(err.message || 'Export error', '⚠️');
  }
}

async function startDirectPptxDownload() {
  const req = collectPresentationRequest();
  if (!req) return;

  showLoading('Generating PPTX…', 'Crafting tables, metric flows, and packaging PowerPoint slides');

  try {
    const res = await fetch('/api/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });

    if (!res.ok) throw new Error('Failed to generate presentation');

    const disposition = res.headers.get('Content-Disposition') || '';
    const match = disposition.match(/filename="?([^"]+)"?/);
    const filename = match ? match[1] : `${sanitizeFilename(req.topic)}_lkyslides.pptx`;

    const blob = await res.blob();
    triggerFileDownload(blob, filename);

    hideLoading();
    showToast('Downloaded .pptx successfully!', '🎉');

  } catch (err) {
    hideLoading();
    showToast(err.message || 'Generation failed', '⚠️');
  }
}

function triggerFileDownload(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 6000);
}

// ── Loading & Notifications ──────────────────────────────────────────────────
let loadingInterval = null;

function showLoading(title, sub) {
  $('loadingMainTitle').textContent = title;
  $('loadingSubText').textContent = sub;
  $('loadingBarFill').style.width = '15%';
  $('gammaLoadingOverlay').hidden = false;

  const steps = $$('.l-step');
  steps.forEach((s) => s.classList.remove('active', 'done'));
  if (steps[0]) steps[0].classList.add('active');

  let pct = 20;
  let step = 0;
  clearInterval(loadingInterval);
  loadingInterval = setInterval(() => {
    pct = Math.min(92, pct + 18);
    $('loadingBarFill').style.width = `${pct}%`;
    if (step < steps.length - 1) {
      steps[step].classList.remove('active');
      steps[step].classList.add('done');
      step++;
      steps[step].classList.add('active');
    }
  }, 750);
}

function hideLoading() {
  clearInterval(loadingInterval);
  $('loadingBarFill').style.width = '100%';
  setTimeout(() => {
    $('gammaLoadingOverlay').hidden = true;
  }, 400);
}

function showToast(message, glyph = '🎉') {
  $('toastGlyph').textContent = glyph;
  $('toastMessage').textContent = message;
  $('gammaToast').hidden = false;
  setTimeout(hideToast, 6500);
}

function hideToast() {
  $('gammaToast').hidden = true;
}

function sanitizeFilename(str) {
  return (str || 'presentation')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '_')
    .replace(/_+/g, '_')
    .slice(0, 45);
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
