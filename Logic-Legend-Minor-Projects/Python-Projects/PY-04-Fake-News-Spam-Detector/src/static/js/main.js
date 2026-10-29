// Logic Legend - PY-04 Fake News and Spam Detector JavaScript Controller

let currentDomain = 'news';

document.addEventListener('DOMContentLoaded', () => {
  renderSamplePills();
  const textInput = document.getElementById('inputText');
  textInput.addEventListener('input', () => {
    document.getElementById('charCount').textContent = `${textInput.value.length} characters`;
  });
});

function switchDomain(domain) {
  currentDomain = domain;
  document.getElementById('tabNews').classList.toggle('active', domain === 'news');
  document.getElementById('tabSpam').classList.toggle('active', domain === 'spam');

  const placeholder = domain === 'news'
    ? "Paste news headline, breaking article excerpt, or viral claim to verify (Real vs Fake)..."
    : "Paste SMS, WhatsApp forward, or email body to classify (Spam vs Non-Spam/Ham)...";
  document.getElementById('inputText').placeholder = placeholder;

  renderSamplePills();
  document.getElementById('verdictSection').style.display = 'none';
}

function renderSamplePills() {
  const container = document.getElementById('samplePills');
  container.innerHTML = '';
  const samples = currentDomain === 'news' ? NEWS_SAMPLES : SPAM_SAMPLES;

  samples.forEach(s => {
    const btn = document.createElement('button');
    btn.className = 'sample-btn';
    btn.textContent = s.title;
    btn.onclick = () => {
      document.getElementById('inputText').value = s.text;
      document.getElementById('charCount').textContent = `${s.text.length} characters`;
    };
    container.appendChild(btn);
  });
}

function clearText() {
  document.getElementById('inputText').value = '';
  document.getElementById('charCount').textContent = '0 characters';
  document.getElementById('verdictSection').style.display = 'none';
}

async function runAnalysis() {
  const text = document.getElementById('inputText').value.trim();
  const model = document.getElementById('modelSelector').value;

  if (!text) {
    alert("Please enter or select sample text to classify.");
    return;
  }

  const verdictSec = document.getElementById('verdictSection');
  verdictSec.style.display = 'block';
  verdictSec.innerHTML = `<div style="text-align: center; padding: 20px; color: #64748B;"><i class="fa-solid fa-spinner fa-spin"></i> Running TF-IDF Vectorization and Model Inference...</div>`;

  try {
    const res = await fetch('/api/predict', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text, domain: currentDomain, model })
    });
    const data = await res.json();

    if (data.error) {
      verdictSec.innerHTML = `<div style="color: red; padding: 10px;">⚠️ ${data.error}</div>`;
      return;
    }

    if (data.mode === 'comparison') {
      renderComparisonView(data.results);
    } else {
      renderSingleResultView(data);
    }
  } catch (err) {
    verdictSec.innerHTML = `<div style="color: red; padding: 10px;">Network error during model inference.</div>`;
  }
}

function renderSingleResultView(data) {
  const verdictSec = document.getElementById('verdictSection');
  const isPositive = ['real', 'ham'].includes(data.prediction.toLowerCase());
  const tagClass = isPositive ? 'tag-real' : 'tag-fake';
  const fillClass = isPositive ? 'fill-green' : 'fill-red';
  const labelDisplay = data.prediction.toUpperCase();

  let probsHtml = '';
  if (data.probabilities) {
    probsHtml = Object.entries(data.probabilities)
      .map(([k, v]) => `<strong>${k.toUpperCase()}:</strong> ${v}%`)
      .join(' &nbsp;|&nbsp; ');
  }

  verdictSec.innerHTML = `
    <div class="verdict-header">
      <div>
        <span style="font-size: 13px; color: #64748B; font-weight: 600;">Prediction Output (${data.model.replace('_', ' ').toUpperCase()}):</span>
        <div class="verdict-tag ${tagClass}" style="margin-top: 4px;">${labelDisplay}</div>
      </div>
      <div style="text-align: right;">
        <span style="font-size: 12px; color: #64748B;">Classification Confidence</span>
        <div style="font-size: 22px; font-weight: 800; color: ${isPositive ? '#15803D' : '#B91C1C'};">${data.confidence}%</div>
      </div>
    </div>

    <div class="confidence-bar-container">
      <div class="confidence-fill ${fillClass}" style="width: ${data.confidence}%;"></div>
    </div>

    <div style="font-size: 12px; color: #475569; margin: 8px 0;">
      Class Probability Distribution: ${probsHtml}
    </div>

    <div class="disclaimer-box">
      <i class="fa-solid fa-circle-info"></i> ${data.disclaimer}
    </div>
  `;
}

function renderComparisonView(results) {
  const verdictSec = document.getElementById('verdictSection');
  let cardsHtml = '';

  for (const [mName, res] of Object.entries(results)) {
    const isPositive = ['real', 'ham'].includes(res.prediction.toLowerCase());
    const tagColor = isPositive ? '#15803D' : '#B91C1C';
    cardsHtml += `
      <div class="comp-card">
        <h4>${mName.replace('_', ' ').toUpperCase()}</h4>
        <div style="font-size: 18px; font-weight: 800; color: ${tagColor}; margin: 6px 0;">
          ${res.prediction.toUpperCase()}
        </div>
        <div style="font-size: 12px; color: #64748B;">Confidence: <strong>${res.confidence}%</strong></div>
      </div>
    `;
  }

  verdictSec.innerHTML = `
    <h3><i class="fa-solid fa-bolt"></i> Cross-Model Comparison Consensus</h3>
    <div class="comparison-grid">${cardsHtml}</div>
    <div class="disclaimer-box" style="margin-top: 15px;">
      <i class="fa-solid fa-circle-info"></i> Running all 3 classifiers provides robustness against edge-case classification bias.
    </div>
  `;
}
