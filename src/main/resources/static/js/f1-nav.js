/* Common F1 navigation; do not duplicate this header in each designer page. */
const navigation=document.getElementById('f1-nav');
if(navigation){
  const links=[['Designer Home','designer-home.html'],['Company','designer-company.html'],['My Projects','designer-idps.html'],['My Profile','profile.html']];
  const current=location.pathname.split('/').pop();
  const header=document.createElement('header');header.className='site-header';
  const inner=document.createElement('div');inner.className='nav-inner';
  const brand=document.createElement('a');brand.className='brand';brand.href='designer-home.html';brand.textContent='IDP.';inner.append(brand);
  const nav=document.createElement('nav');nav.className='nav-links';nav.setAttribute('aria-label','Designer navigation');
  for(const [name,href] of links){const a=document.createElement('a');a.href=href;a.textContent=name;if(href===current){a.className='active';a.setAttribute('aria-current','page');}nav.append(a);}
  const logout=document.createElement('button');logout.type='button';logout.textContent='Log out';
  logout.addEventListener('click',async()=>{logout.disabled=true;try{await f1Request(F1_ENDPOINTS.logout,{method:'POST'});location.href='/login.html';}catch(err){alert(err.message);logout.disabled=false;}});
  nav.append(logout);inner.append(nav);header.append(inner);navigation.append(header);
}
