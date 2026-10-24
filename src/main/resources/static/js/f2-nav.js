/* F2 navigation bar for customer, platform manager and guest pages.*/

(function () {
  const holder = document.getElementById('f2-nav');
  if (!holder) return;

  const ROLES = {
    customer: {
      home: 'customer-home.html', label: 'Customer navigation', logout: true,
      links: [['Home', 'customer-home.html'], ['Browse Projects', 'customer-browse-idp.html'],
              ['My Favourites', 'customer-favourites.html'], ['My Profile', 'customer-profile.html']]
    },
    platform: {
      home: 'platform-home.html', label: 'Platform manager navigation', logout: true,
      links: [['Dashboard', 'platform-home.html'], ['Categories', 'platform-categories.html'],
              ['Reports', 'platform-reports.html'], ['Insights', 'platform-insights.html']]
    },

    guest: {
      home: 'guest-home.html', label: 'Guest navigation', logout: false,
      links: [['Browse Projects', 'guest-home.html'], ['Log In', 'login.html'],
              ['Register', 'customer-register.html']]
    }
  };

  const role = ROLES[holder.dataset.role];
  if (!role) return;
  const current = holder.dataset.active || location.pathname.split('/').pop() || role.home;

  const header = document.createElement('header');
  header.className = 'site-header';
  const inner = document.createElement('div');
  inner.className = 'nav-inner';

  const brand = document.createElement('a');
  brand.className = 'brand';
  brand.href = role.home;
  brand.textContent = 'IDP.';
  inner.append(brand);

  const nav = document.createElement('nav');
  nav.className = 'nav-links';
  nav.setAttribute('aria-label', role.label);

  for (const [name, href] of role.links) {
    const a = document.createElement('a');
    a.href = href;
    a.textContent = name;
    if (href === current) {
      a.className = 'active';
      a.setAttribute('aria-current', 'page');
    }
    nav.append(a);
  }

  if (role.logout) {
    const logout = document.createElement('button');
    logout.type = 'button';
    logout.textContent = 'Log out';
    logout.addEventListener('click', async () => {
      logout.disabled = true;
      try { await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' }); }
      finally { location.href = 'login.html'; }
    });
    nav.append(logout);
  }

  inner.append(nav);
  header.append(inner);
  holder.append(header);
})();