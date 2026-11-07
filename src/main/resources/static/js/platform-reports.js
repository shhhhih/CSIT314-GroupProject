document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('reportForm');
  const dateInput = document.getElementById('reportDate');
  const monthInput = document.getElementById('reportMonth');
  const now = new Date();
  const localDate = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  dateInput.value = localDate(now);
  monthInput.value = localDate(now).slice(0,7);
  const update = (id, value) => { document.getElementById(id).textContent = value; };
  async function getDaily(date) {
    const response = await fetch('/api/reports/daily?date=' + encodeURIComponent(date), {credentials:'same-origin'});
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || `Report request failed (${response.status})`);
    return data;
  }
  function datesFor(type) {
    if(type==='daily') return [dateInput.value];
    let first, count;
    if(type==='weekly') {
      first=new Date(dateInput.value+'T12:00:00');
      first.setDate(first.getDate()-((first.getDay()+6)%7));
      count=7;
    } else {
      const [y,m]=monthInput.value.split('-').map(Number);
      if (!y || !m) throw new Error('Select a month');
      first=new Date(y,m-1,1,12); count=new Date(y,m,0).getDate();
    }
    const dates=[];
    for(let i=0;i<count;i++) {
      const d=new Date(first);d.setDate(d.getDate()+i);dates.push(localDate(d));
    }
    return dates;
  }
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const button=form.querySelector('button[type=submit]');
    button.disabled=true;
    update('period','Generating report...');
    try {
      const type=document.getElementById('reportType').value;
      const dates=datesFor(type);
      const reports=await Promise.all(dates.map(getDaily));
      update('resultType',type.charAt(0).toUpperCase()+type.slice(1)+' report');
      update('period',dates.length===1?dates[0]:`${dates[0]} to ${dates.at(-1)}`);
      const total=key=>reports.reduce((acc,r)=>acc+r[key],0);
      update('newUsers',total('newUsers'));
      update('newIdps',total('newIdps'));
      update('totalViews',total('totalViews'));
      update('totalShortlists',total('totalShortlists'));
      const counts=new Map();
      reports.forEach(r=>r.topIdps.forEach(p=>{
        const previous=counts.get(p.idpId)||{title:p.title,views:0};
        previous.views+=p.views;counts.set(p.idpId,previous);
      }));
      const top=[...counts.values()].sort((a,b)=>b.views-a.views).slice(0,5);
      const rows=document.getElementById('topRows');rows.replaceChildren();
      if(!top.length) {const tr=rows.insertRow();const td=tr.insertCell();td.colSpan=3;td.textContent='No views recorded for this period.';}
      top.forEach((p,i)=>{const tr=rows.insertRow();[i+1,p.title,p.views].forEach(value=>{tr.insertCell().textContent=value;});});
      const dayRows=document.getElementById('dayRows');dayRows.replaceChildren();
      reports.forEach(r=>{const tr=dayRows.insertRow();
        [r.date,r.newUsers,r.newIdps,r.totalViews,r.totalShortlists].forEach(value=>tr.insertCell().textContent=value);
      });
    } catch(error) {update('period',error.message);}
    finally {button.disabled=false;}
  });
});
