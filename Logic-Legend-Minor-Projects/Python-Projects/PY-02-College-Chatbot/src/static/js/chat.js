// Logic Legend - PY-02 College Chatbot JavaScript Controller

const messagesContainer = document.getElementById('chatMessages');
const userInput = document.getElementById('userInput');
const escalateModal = document.getElementById('escalateModal');
let isRecording = false;
let recognition = null;

// Initialize Web Speech API if supported
if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = false;
  recognition.lang = 'en-US';

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    userInput.value = transcript;
    toggleVoiceInput();
    handleSend();
  };

  recognition.onerror = () => {
    toggleVoiceInput();
  };
}

function toggleVoiceInput() {
  const micBtn = document.getElementById('micBtn');
  if (!recognition) {
    alert("Speech recognition is not supported in this browser. Please type your query.");
    return;
  }
  if (!isRecording) {
    recognition.start();
    isRecording = true;
    micBtn.classList.add('recording');
  } else {
    recognition.stop();
    isRecording = false;
    micBtn.classList.remove('recording');
  }
}

function sendSuggested(query) {
  userInput.value = query;
  handleSend();
}

async function handleSend(e) {
  if (e) e.preventDefault();
  const text = userInput.value.trim();
  if (!text) return;

  userInput.value = '';
  appendUserMessage(text);

  // Show typing placeholder
  const typingId = appendTypingIndicator();

  try {
    const res = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: text })
    });
    const data = await res.json();
    removeElement(typingId);
    appendBotMessage(data);
  } catch (err) {
    removeElement(typingId);
    appendBotMessage({
      response: "Unable to reach college chatbot server. Please check your connection or contact admissions@logiclegend.edu.",
      verified: false,
      timestamp: "Now",
      suggest_escalation: true
    });
  }
}

function appendUserMessage(text) {
  const row = document.createElement('div');
  row.className = 'message-row user-row';
  row.innerHTML = `
    <div class="avatar-mini"><i class="fa-solid fa-user"></i></div>
    <div class="message-bubble user-bubble">
      <p>${escapeHtml(text)}</p>
      <div class="meta-info">
        <span class="msg-time">Just now</span>
      </div>
    </div>
  `;
  messagesContainer.appendChild(row);
  scrollToBottom();
}

function appendBotMessage(data) {
  const row = document.createElement('div');
  row.className = 'message-row bot-row';

  const badgeHtml = data.verified
    ? `<span class="verified-badge"><i class="fa-solid fa-check-circle"></i> Official Verified (Confidence: ${Math.round((data.confidence || 1) * 100)}%)</span>`
    : `<span class="unverified-badge"><i class="fa-solid fa-triangle-exclamation"></i> Unverified / Fallback</span>`;

  let escalationHtml = '';
  if (data.suggest_escalation) {
    escalationHtml = `
      <div style="margin-top: 10px;">
        <button onclick="openEscalateModal('${escapeHtml(userInput.value || '')}')" class="btn-escalate" style="font-size: 11px; padding: 4px 8px;">
          <i class="fa-solid fa-headset"></i> Connect with Administrator
        </button>
      </div>
    `;
  }

  row.innerHTML = `
    <div class="avatar-mini"><i class="fa-solid fa-robot"></i></div>
    <div class="message-bubble bot-bubble">
      <p>${escapeHtml(data.response)}</p>
      ${escalationHtml}
      <div class="meta-info">
        ${badgeHtml}
        <span class="msg-time">${data.timestamp || 'Just now'}</span>
      </div>
    </div>
  `;
  messagesContainer.appendChild(row);
  scrollToBottom();
}

function appendTypingIndicator() {
  const id = 'typing_' + Date.now();
  const row = document.createElement('div');
  row.id = id;
  row.className = 'message-row bot-row';
  row.innerHTML = `
    <div class="avatar-mini"><i class="fa-solid fa-robot"></i></div>
    <div class="message-bubble bot-bubble" style="color: #94A3B8; font-style: italic;">
      Consulting college knowledge base...
    </div>
  `;
  messagesContainer.appendChild(row);
  scrollToBottom();
  return id;
}

function removeElement(id) {
  const el = document.getElementById(id);
  if (el) el.remove();
}

function scrollToBottom() {
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function escapeHtml(text) {
  const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
  return text.replace(/[&<>"']/g, m => map[m]);
}

// Modal Handlers
document.getElementById('btnEscalate').addEventListener('click', () => openEscalateModal(''));

function openEscalateModal(defaultQuery) {
  if (defaultQuery) {
    document.getElementById('escQuery').value = defaultQuery;
  }
  escalateModal.style.display = 'flex';
}

function closeEscalateModal() {
  escalateModal.style.display = 'none';
}

async function submitEscalation(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('escName').value.trim(),
    email: document.getElementById('escEmail').value.trim(),
    phone: document.getElementById('escPhone').value.trim(),
    query: document.getElementById('escQuery').value.trim()
  };

  try {
    const res = await fetch('/api/escalate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      alert(data.message);
      closeEscalateModal();
      document.getElementById('escalateForm').reset();
    } else {
      alert("Error submitting ticket: " + (data.error || 'Server error'));
    }
  } catch (err) {
    alert("Network error submitting escalation.");
  }
}

