document.addEventListener('DOMContentLoaded',()=>{
  const rows=document.getElementById('rows');
  const search=document.getElementById('search');
  const form=search.closest('form');
  async function load(){
    const param=search.value.trim();
    const response=await fetch('/api/categories'+(param?'?search='+encodeURIComponent(param):''),{credentials:'same-origin'});
    if(!response.ok) throw new Error(`Unable to load categories (${response.status})`);
    const categories=await response.json();
    rows.replaceChildren();
    if(!categories.length){const tr=rows.insertRow();const td=tr.insertCell();td.colSpan=4;td.textContent='No matching categories.';return;}
    categories.forEach(c=>{
      const tr=rows.insertRow();
      const td=tr.insertCell();const link=document.createElement('a');
      link.href='platform-category.html?id='+encodeURIComponent(c.categoryID);link.textContent=c.categoryName;
      td.append(link);
      tr.insertCell().textContent=c.description||'—';
      tr.insertCell().textContent=c.createdAt?new Date(c.createdAt).toLocaleDateString('en-SG'):'—';
      const actions=tr.insertCell();
      const edit=document.createElement('a');edit.href='platform-category-form.html?id='+encodeURIComponent(c.categoryID);edit.textContent='Edit';actions.append(edit);
    });
  }
  form.addEventListener('submit',e=>{e.preventDefault();load().catch(e=>alert(e.message));});
  load().catch(e=>{rows.replaceChildren();rows.insertRow().insertCell().textContent=e.message;});
});
