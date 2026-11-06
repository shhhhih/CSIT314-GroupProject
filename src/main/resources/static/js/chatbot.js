
(() => {
  "use strict";

  if (document.getElementById("idp-smart-chatbot")) {
    return;
  }

  const styles = document.createElement("style");

  styles.textContent = `
    #idp-smart-chatbot {
      position: fixed;
      right: 24px;
      bottom: 24px;
      z-index: 10000;
      font-family: "Segoe UI", Arial, sans-serif;
      color: #27332c;
    }

    #idp-smart-chatbot * {
      box-sizing: border-box;
    }

    .idp-bot-window {
      display: none;
      flex-direction: column;
      width: min(540px, calc(100vw - 32px));
      height: min(710px, calc(100dvh - 105px));
      margin-bottom: 12px;
      background: #ffffff;
      border: 1px solid #dfe7df;
      border-radius: 22px;
      overflow: hidden;
      box-shadow: 0 18px 55px rgba(0,0,0,.20);
    }

    .idp-bot-window.open {
      display: flex;
      animation: idp-bot-appear .22s ease-out;
    }

    @keyframes idp-bot-appear {
      from {
        opacity: 0;
        transform: translateY(12px);
      }

      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .idp-bot-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 20px;
      background: #496652;
      color: #ffffff;
    }

    .idp-bot-header strong {
      display: block;
      font-size: 18px;
      font-weight: 700;
    }

    .idp-bot-header small {
      display: block;
      margin-top: 5px;
      font-size: 12px;
      opacity: .9;
    }

    .idp-bot-header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .idp-bot-header-actions button {
      border: none;
      border-radius: 9px;
      padding: 9px 11px;
      background: rgba(255,255,255,.15);
      color: white;
      font: inherit;
      font-size: 13px;
      cursor: pointer;
    }

    .idp-bot-header-actions button:hover {
      background: rgba(255,255,255,.25);
    }

    .idp-bot-close {
      font-size: 22px !important;
      line-height: 1;
    }

    .idp-bot-messages {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      gap: 13px;
      overflow-y: auto;
      padding: 20px;
      background: #f7f6f2;
      scroll-behavior: smooth;
    }

    .idp-bot-message {
      max-width: 88%;
      padding: 14px 17px;
      border-radius: 16px;
      font-size: 15px;
      line-height: 1.65;
      overflow-wrap: anywhere;
      white-space: pre-wrap;
    }

    .idp-bot-message.bot {
      align-self: flex-start;
      background: white;
      border: 1px solid #dfe7df;
      border-bottom-left-radius: 5px;
    }

    .idp-bot-message.user {
      align-self: flex-end;
      background: #496652;
      color: white;
      border-bottom-right-radius: 5px;
    }

    .idp-bot-message.loading {
      color: #64736a;
      font-style: italic;
    }

    .idp-bot-project {
      display: flex;
      flex-direction: column;
      align-self: flex-start;
      gap: 8px;
      width: 94%;
      padding: 15px;
      border: 1px solid #dfe7df;
      border-radius: 13px;
      background: white;
      text-decoration: none;
      color: #27332c;
      transition:
        transform .2s ease,
        border-color .2s ease;
    }

    .idp-bot-project:hover {
      transform: translateY(-2px);
      border-color: #496652;
    }

    .idp-bot-project strong {
      font-size: 15px;
      color: #304a39;
    }

    .idp-bot-project span {
      font-size: 13px;
      line-height: 1.5;
      color: #68746b;
    }

    .idp-bot-project em {
      font-size: 14px;
      font-style: normal;
      font-weight: bold;
      color: #496652;
    }

    .idp-bot-quick {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding: 12px 15px;
      max-height: 125px;
      overflow-y: auto;
      background: white;
      border-top: 1px solid #edf0ec;
    }

    .idp-bot-quick button {
      padding: 9px 12px;
      border: 1px solid #dfe7df;
      border-radius: 20px;
      background: #edf3ed;
      color: #304a39;
      font-family: inherit;
      font-size: 13px;
      cursor: pointer;
    }

    .idp-bot-quick button:hover {
      background: #dce9dd;
    }

    .idp-bot-quick button:disabled {
      opacity: .5;
      cursor: wait;
    }

    .idp-bot-form {
      display: flex;
      gap: 10px;
      padding: 14px;
      background: white;
      border-top: 1px solid #edf0ec;
    }

    .idp-bot-input {
      flex: 1;
      min-width: 0;
      padding: 13px 15px;
      border: 1px solid #cdd9ce;
      border-radius: 12px;
      background: white;
      color: #27332c;
      font-family: inherit;
      font-size: 15px;
    }

    .idp-bot-input:focus {
      outline: 2px solid #a7c1ae;
      outline-offset: 1px;
    }

    .idp-bot-send {
      padding: 0 18px;
      border: none;
      border-radius: 12px;
      background: #496652;
      color: white;
      font-family: inherit;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
    }

    .idp-bot-send:hover {
      background: #304a39;
    }

    .idp-bot-send:disabled {
      opacity: .5;
      cursor: wait;
    }

    .idp-bot-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 66px;
      height: 66px;
      margin-left: auto;
      border: none;
      border-radius: 50%;
      background: #496652;
      color: white;
      font-size: 29px;
      cursor: pointer;
      box-shadow: 0 8px 28px rgba(0,0,0,.23);
      transition:
        transform .2s ease,
        background .2s ease;
    }

    .idp-bot-toggle:hover {
      background: #304a39;
      transform: translateY(-3px) scale(1.05);
    }

    .idp-bot-tooltip {
      position: absolute;
      right: 78px;
      bottom: 13px;
      padding: 11px 15px;
      border: 1px solid #dfe7df;
      border-radius: 13px;
      white-space: nowrap;
      background: white;
      color: #304a39;
      box-shadow: 0 5px 18px rgba(0,0,0,.13);
      font-size: 13px;
      opacity: 0;
      transform: translateX(8px);
      pointer-events: none;
      transition:
        opacity .2s ease,
        transform .2s ease;
    }

    .idp-bot-toggle:hover + .idp-bot-tooltip,
    .idp-bot-toggle:focus-visible + .idp-bot-tooltip {
      opacity: 1;
      transform: translateX(0);
    }

    .idp-bot-window.open ~ .idp-bot-tooltip {
      display: none;
    }

    @media (max-width: 600px) {
      #idp-smart-chatbot {
        right: 16px;
        bottom: 16px;
      }

      .idp-bot-window {
        width: calc(100vw - 32px);
        height: min(680px, calc(100dvh - 100px));
        border-radius: 18px;
      }

      .idp-bot-header {
        padding: 15px;
      }

      .idp-bot-header strong {
        font-size: 16px;
      }

      .idp-bot-messages {
        padding: 14px;
      }

      .idp-bot-message {
        font-size: 14px;
      }

      .idp-bot-quick {
        flex-wrap: nowrap;
        overflow-x: auto;
        overflow-y: hidden;
        max-height: none;
      }

      .idp-bot-quick button {
        flex-shrink: 0;
      }

      .idp-bot-tooltip {
        right: 76px;
        font-size: 12px;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      #idp-smart-chatbot * {
        animation: none !important;
        transition: none !important;
        scroll-behavior: auto !important;
      }
    }
  `;

  document.head.appendChild(styles);

  const root = document.createElement("div");
  root.id = "idp-smart-chatbot";

  root.innerHTML = `
    <section
      id="idp-bot-window"
      class="idp-bot-window"
      role="dialog"
      aria-label="IDP Smart Renovation Assistant"
    >
      <header class="idp-bot-header">
        <div>
          <strong>🏡 IDP Smart Assistant</strong>
          <small>Your friendly renovation helper 💚</small>
        </div>

        <div class="idp-bot-header-actions">
          <button
            id="idp-bot-reset"
            type="button"
            title="Start a new conversation"
            aria-label="Restart conversation"
          >↻ Reset</button>

          <button
            id="idp-bot-close"
            class="idp-bot-close"
            type="button"
            aria-label="Close chat"
          >×</button>
        </div>
      </header>

      <div
        id="idp-bot-messages"
        class="idp-bot-messages"
        aria-live="polite"
        aria-relevant="additions"
      ></div>

      <div
        id="idp-bot-quick"
        class="idp-bot-quick"
        aria-label="Suggested replies"
      ></div>

      <form id="idp-bot-form" class="idp-bot-form">
        <input
          id="idp-bot-input"
          class="idp-bot-input"
          type="text"
          maxlength="500"
          autocomplete="off"
          placeholder="Ask me about your renovation..."
          aria-label="Type your message"
          required
        >

        <button
          id="idp-bot-send"
          class="idp-bot-send"
          type="submit"
        >Send</button>
      </form>
    </section>

    <button
      id="idp-bot-toggle"
      class="idp-bot-toggle"
      type="button"
      aria-label="Open chatbot"
      aria-expanded="false"
      aria-controls="idp-bot-window"
    >💬</button>

    <div class="idp-bot-tooltip" aria-hidden="true">
      Hi there! 👋 Chat with me
    </div>
  `;

  document.body.appendChild(root);

  const chatWindow = root.querySelector("#idp-bot-window");
  const toggle = root.querySelector("#idp-bot-toggle");
  const closeButton = root.querySelector("#idp-bot-close");
  const resetButton = root.querySelector("#idp-bot-reset");
  const messages = root.querySelector("#idp-bot-messages");
  const quick = root.querySelector("#idp-bot-quick");
  const form = root.querySelector("#idp-bot-form");
  const input = root.querySelector("#idp-bot-input");
  const sendButton = root.querySelector("#idp-bot-send");

  let context = {};
  let busy = false;

  const welcomeText =
    "Hey there! 👋💚 Welcome to IDP!\n\n" +
    "I'm your friendly renovation assistant. 🏡✨ " +
    "I can help you explore room makeovers, understand " +
    "renovation budgets, and discover interior design " +
    "projects.\n\n" +
    "Don't worry if you're unsure about anything! " +
    "We can figure it out together. 🌷\n\n" +
    "What would you like to renovate?";

  const initialReplies = [
    "Kitchen",
    "Bathroom",
    "Guest room",
    "Garden",
    "Find projects"
  ];

  function scrollToBottom() {
    messages.scrollTop = messages.scrollHeight;
  }

  function addMessage(text, sender, extraClass = "") {
    const bubble = document.createElement("div");

    bubble.className =
      `idp-bot-message ${sender} ${extraClass}`.trim();

    // Never interpret customer text as HTML.
    bubble.textContent = String(text);

    messages.appendChild(bubble);
    scrollToBottom();

    return bubble;
  }

  function showQuickReplies(items) {
    quick.replaceChildren();

    if (!Array.isArray(items)) {
      return;
    }

    for (const label of items) {
      if (typeof label !== "string") {
        continue;
      }

      const button = document.createElement("button");
      button.type = "button";
      button.textContent = label;
      button.disabled = busy;

      button.addEventListener("click", () => {
        sendMessage(label);
      });

      quick.appendChild(button);
    }
  }

  function addProject(project) {
    if (!project || project.idpId == null) {
      return;
    }

    const card = document.createElement("a");
    card.className = "idp-bot-project";

    // Confirm this path/query parameter against your
    // team's actual customer project detail page.
    card.href =
      `customer-idp.html?id=${encodeURIComponent(project.idpId)}`;

    const title = document.createElement("strong");
    title.textContent =
      project.title || "Interior Design Project";

    const details = document.createElement("span");
    details.textContent = [
      project.propertyType,
      project.categoryName
    ].filter(Boolean).join(" · ") || "Interior Design";

    const price = document.createElement("em");
    const amount = Number(project.budget);

    price.textContent =
      project.budget != null && Number.isFinite(amount)
        ? `Listed budget: S$${amount.toLocaleString("en-SG")}`
        : "Budget not specified";

    const action = document.createElement("span");
    action.textContent = "View project details →";

    card.append(title, details, price, action);
    messages.appendChild(card);

    scrollToBottom();
  }

  async function sendMessage(question) {
    const value = String(question || "").trim();

    if (!value || busy) {
      return;
    }

    busy = true;
    sendButton.disabled = true;

    quick.querySelectorAll("button").forEach(button => {
      button.disabled = true;
    });

    input.value = "";
    addMessage(value, "user");

    const loading = addMessage(
      "Let me check that for you... 💚",
      "bot",
      "loading"
    );

    try {
      const response = await fetch("/api/chatbot/message", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "same-origin",
        body: JSON.stringify({
          message: value,
          context: context
        })
      });

      if (!response.ok) {
        throw new Error(
          `Chatbot API returned HTTP ${response.status}`
        );
      }

      const data = await response.json();

      loading.remove();

      if (
        data.context &&
        typeof data.context === "object" &&
        !Array.isArray(data.context)
      ) {
        context = data.context;
      }

      addMessage(
        data.reply ||
        "I'm sorry, I didn't quite understand. 💚 " +
        "Could you tell me a little more?",
        "bot"
      );

      if (Array.isArray(data.projects)) {
        data.projects.forEach(addProject);
      }

      if (Array.isArray(data.quickReplies)) {
        showQuickReplies(data.quickReplies);
      } else {
        showQuickReplies([]);
      }

    } catch (error) {
      loading.remove();

      console.error("IDP chatbot error:", error);

      addMessage(
        "Oops, I'm having a little trouble connecting " +
        "right now. 💚 Please try again in a moment.",
        "bot"
      );

      showQuickReplies([
        "Find projects",
        "Start over"
      ]);

    } finally {
      busy = false;
      sendButton.disabled = false;

      quick.querySelectorAll("button").forEach(button => {
        button.disabled = false;
      });

      scrollToBottom();
      input.focus();
    }
  }

  function resetChat() {
    if (busy) {
      return;
    }

    context = {};
    messages.replaceChildren();

    addMessage(welcomeText, "bot");
    showQuickReplies(initialReplies);

    input.value = "";
    input.focus();
  }

  function setOpen(open) {
    chatWindow.classList.toggle("open", open);

    toggle.setAttribute(
      "aria-expanded",
      String(open)
    );

    toggle.setAttribute(
      "aria-label",
      open ? "Close chatbot" : "Open chatbot"
    );

    if (open) {
      input.focus();
      scrollToBottom();
    } else {
      toggle.focus();
    }
  }

  toggle.addEventListener("click", () => {
    setOpen(!chatWindow.classList.contains("open"));
  });

  closeButton.addEventListener("click", () => {
    setOpen(false);
  });

  resetButton.addEventListener("click", resetChat);

  form.addEventListener("submit", event => {
    event.preventDefault();
    sendMessage(input.value);
  });

  document.addEventListener("keydown", event => {
    if (
      event.key === "Escape" &&
      chatWindow.classList.contains("open")
    ) {
      setOpen(false);
    }
  });

  addMessage(welcomeText, "bot");
  showQuickReplies(initialReplies);
})();


