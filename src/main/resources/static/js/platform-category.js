document.addEventListener('DOMContentLoaded',async()=>{
 const id=new URLSearchParams(location.search).get('id');
 const set=(key,value)=>{document.getElementById(key).textContent=value??'—';};
 if(!id){set('categoryName','Missing category ID');return;}
 try{
  const [categoryResponse,projectsResponse]=await Promise.all([
    fetch('/api/categories/'+encodeURIComponent(id)),
    fetch('/api/guest/idps?categoryId='+encodeURIComponent(id))
  ]);
  if(!categoryResponse.ok) throw new Error('Unable to load category');
  const c=await categoryResponse.json();
  set('categoryName',c.categoryName);set('description',c.description||'No description.');
  set('createdAt',c.createdAt?new Date(c.createdAt).toLocaleDateString('en-SG'):'—');
  // This public count includes published IDPs only; delete is protected by the backend against all IDPs.
  if(projectsResponse.ok) set('idpCount',(await projectsResponse.json()).length+' published');
  document.getElementById('editLink').href='platform-category-form.html?id='+encodeURIComponent(id);
  document.getElementById('deleteBtn').addEventListener('click',async()=>{
    if(!confirm('Delete this category? This is only allowed if no IDP uses it.'))return;
    const response=await fetch('/api/categories/'+encodeURIComponent(id),{method:'DELETE',credentials:'same-origin'});
    if(response.ok){location.href='platform-categories.html';return;}
    const error=await response.json().catch(()=>({}));alert(error.message||'Delete failed');
  });
 }catch(e){set('categoryName',e.message);}
});
