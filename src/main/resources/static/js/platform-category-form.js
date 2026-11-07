document.addEventListener('DOMContentLoaded',async()=>{
 const form=document.getElementById('categoryForm');
 const id=new URLSearchParams(location.search).get('id');
 if(id){
  document.getElementById('heading').textContent='Edit Category';
  try{const response=await fetch('/api/categories/'+encodeURIComponent(id));if(!response.ok)throw new Error('Category not found');
    const c=await response.json();form.elements.categoryName.value=c.categoryName;form.elements.description.value=c.description||'';
  }catch(e){alert(e.message);}
 }
 form.addEventListener('submit',async e=>{
  e.preventDefault();if(!form.reportValidity())return;
  const button=form.querySelector('button[type=submit]');button.disabled=true;
  try{
   const response=await fetch('/api/categories'+(id?'/'+encodeURIComponent(id):''),{
     method:id?'PUT':'POST',credentials:'same-origin',headers:{'Content-Type':'application/json'},
     body:JSON.stringify({categoryName:form.elements.categoryName.value.trim(),description:form.elements.description.value.trim()})
   });
   const data=await response.json().catch(()=>({}));
   if(!response.ok)throw new Error(data.message||'Unable to save category');
   location.href='platform-categories.html';
  }catch(error){alert(error.message);button.disabled=false;}
 });
});
