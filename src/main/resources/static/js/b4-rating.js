/* B4: "Rate this design" section on customer-idp.html?id=IDP_ID.
   Adds its own card to the page, so customer-idp.html only needs: <script src="js/b4-rating.js"></script>
   API: GET /api/ratings/idp/{idpId} (logged in), POST /api/ratings (Customer). */
(function () {
  const idpId = new URLSearchParams(location.search).get('id');
  if (!idpId) return;

  const style = document.createElement('style');
  style.textContent =
    '.b4-summary{display:flex;align-items:baseline;gap:10px;margin-bottom:18px}' +
    '.b4-average{font-family:Georgia,serif;font-size:34px}' +
    '.b4-stars{color:#c8952c;letter-spacing:2px}' +
    '.b4-picker{display:flex;gap:4px;margin-bottom:14px}' +
    '.b4-picker button{border:0;background:none;font-size:30px;line-height:1;color:#cdd9ce;padding:0 2px}' +
    '.b4-picker button.on{color:#c8952c}' +
    '.b4-list{list-style:none;margin:20px 0 0;padding:0;display:grid;gap:14px}' +
    '.b4-list li{border-top:1px solid var(--line);padding-top:14px}' +
    '.b4-list p{margin:6px 0 0}' +
    '.b4-form textarea{min-height:90px}';
  document.head.appendChild(style);

  // ---- build the card (same classes as the rest of the page) ----
  const card = document.createElement('section');
  card.className = 'card';
  card.id = 'ratings';
  card.innerHTML =
    '<h2>Ratings</h2>' +
    '<div class="b4-summary"><span class="b4-average" id="b4Average">–</span>' +
    '<span class="b4-stars" id="b4AverageStars"></span><span class="muted" id="b4Count"></span></div>' +
    '<p class="alert" id="b4Alert" hidden></p>' +
    '<form class="b4-form" id="b4Form" hidden>' +
    '  <div class="field"><label>Your rating</label><div class="b4-picker" id="b4Picker" role="radiogroup" aria-label="Stars"></div></div>' +
    '  <div class="field"><label for="b4Comment">Comment (optional)</label>' +
    '  <textarea id="b4Comment" maxlength="500" placeholder="What did you like about this design?"></textarea>' +
    '  <small><span id="b4Chars">0</span>/500</small></div>' +
    '  <button class="btn" type="submit" id="b4Submit">Submit rating</button>' +
    '</form>' +
    '<ul class="b4-list" id="b4List"></ul>';
  const leftColumn = document.querySelector('.detail-layout .stack');
  (leftColumn || document.querySelector('main') || document.body).appendChild(card);

  const $ = (id) => document.getElementById(id);
  const starsText = (n) => '★'.repeat(n) + '☆'.repeat(5 - n);
  let chosen = 0;

  function showAlert(text, type) {
    const box = $('b4Alert');
    box.textContent = text;
    box.className = 'alert ' + (type || 'error');
    box.hidden = !text;
  }

  async function request(url, options) {
    const response = await fetch(url, Object.assign({ credentials: 'same-origin' }, options));
    let data = {};
    try { data = await response.json(); } catch (e) { /* empty body */ }
    return { status: response.status, ok: response.ok, data: data };
  }

  // ---- star picker ----
  const picker = $('b4Picker');
  for (let i = 1; i <= 5; i++) {
    const star = document.createElement('button');
    star.type = 'button';
    star.textContent = '★';
    star.setAttribute('role', 'radio');
    star.setAttribute('aria-label', i + (i === 1 ? ' star' : ' stars'));
    star.addEventListener('click', () => { chosen = i; paintPicker(); });
    picker.appendChild(star);
  }
  function paintPicker() {
    [...picker.children].forEach((star, index) => {
      star.classList.toggle('on', index < chosen);
      star.setAttribute('aria-checked', String(index + 1 === chosen));
    });
  }
  $('b4Comment').addEventListener('input', (e) => { $('b4Chars').textContent = e.target.value.length; });

  // ---- load ratings ----
  async function loadRatings() {
    const result = await request('/api/ratings/idp/' + encodeURIComponent(idpId));
    if (result.status === 401) {
      $('b4Count').innerHTML = '<a href="login.html">Log in</a> to see and add ratings';
      return;
    }
    if (!result.ok) { showAlert(result.data.message || 'Could not load ratings'); return; }

    const r = result.data;
    $('b4Average').textContent = r.ratingCount ? r.averageStars.toFixed(1) : '–';
    $('b4AverageStars').textContent = r.ratingCount ? starsText(Math.round(r.averageStars)) : '';
    $('b4Count').textContent = r.ratingCount === 0 ? 'No ratings yet'
      : '(' + r.ratingCount + (r.ratingCount === 1 ? ' rating)' : ' ratings)');

    const list = $('b4List');
    list.replaceChildren();
    r.ratings.forEach((rating) => {
      const item = document.createElement('li');
      const stars = document.createElement('span');
      stars.className = 'b4-stars';
      stars.textContent = starsText(rating.stars);
      const date = document.createElement('span');
      date.className = 'muted';
      date.textContent = '  ' + new Date(rating.ratedAt).toLocaleDateString();
      item.append(stars, date);
      if (rating.comment) {
        const comment = document.createElement('p');
        comment.textContent = rating.comment;   // textContent, so comments can't inject HTML
        item.appendChild(comment);
      }
      list.appendChild(item);
    });
  }

  // ---- only customers see the form ----
  async function showFormForCustomers() {
    const me = await request('/api/auth/me');
    if (me.ok && me.data.profileName === 'Customer') $('b4Form').hidden = false;
  }

  // ---- submit ----
  $('b4Form').addEventListener('submit', async (event) => {
    event.preventDefault();
    if (chosen === 0) { showAlert('Please choose 1 to 5 stars'); return; }
    $('b4Submit').disabled = true;
    const result = await request('/api/ratings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idpId: Number(idpId), stars: chosen, comment: $('b4Comment').value })
    });
    $('b4Submit').disabled = false;
    if (result.status === 401) { location.href = 'login.html'; return; }
    if (!result.ok) { showAlert(result.data.message || 'Could not save your rating'); return; }
    showAlert('Thanks! Your rating was saved.', 'success');
    $('b4Submit').textContent = 'Update rating';
    loadRatings();
  });

  loadRatings();
  showFormForCustomers();
})();
