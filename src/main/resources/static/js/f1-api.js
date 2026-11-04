
/* F1 frontend adapter. CONFIRM paths and payloads against your team's API.md before integration.
   Only change this file when B1/B2/B3 finalize their contracts. No sample data is persisted. */

const F1_ENDPOINTS = Object.freeze({
  me: '/api/profile', // confirmed by existing Sprint 1 profile.html
  logout: '/api/logout', // provisional, check B1 contract
  company: '/api/designer/company', // provisional, check B2 contract

  // Existing frontend route (preserved for My Projects / Edit Project)
  idps: '/api/designer/idps',

  // B3 - US6 Create IDP endpoints (tested successfully)
  createIdp: '/api/idps',
  idpCategories: '/api/idps/categories',
  idpImages: '/api/idps/images'
});

async function f1Request(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'same-origin',
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers || {})
    }
  });

  if (response.status === 401) {
    location.href = '/login.html';
    throw new Error('Please log in again.');
  }

  const raw = await response.text();

  let data = {};

  try {
    data = raw ? JSON.parse(raw) : {};
  } catch {
    data = {
      message: raw || 'Invalid response from server'
    };
  }

  if (!response.ok) {
    throw new Error(
      data.message ||
      `Request failed (${response.status}). Confirm the API route in js/f1-api.js.`
    );
  }

  return data;
}

function f1Alert(element, text, type = 'error') {
  element.textContent = text;
  element.className = `alert ${type}`;
  element.hidden = !text;
}

function f1RenderText(id, value) {
  document.getElementById(id).textContent =
    value == null ? '—' : String(value);
}

function f1IdpPath(id) {
  return `${F1_ENDPOINTS.idps}/${encodeURIComponent(id)}`;
}

