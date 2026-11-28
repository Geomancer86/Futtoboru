"""Write a local HTML page that shows the baked match sheets animated (for eyeballing; not committed output).

    python tools/art/match/preview_html.py <out.html>

The page embeds index.json and every PNG as data URIs, so it opens straight from disk.
"""
import base64
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import match_art as m  # noqa: E402

PAGE = r"""<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Baked match sprites</title>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
:root{--bg:#e9e2cf;--fg:#2a2622;--card:#f6f1e4;--line:#c9bd9e;--grass:#5c7842}
body{margin:0;background:var(--bg);color:var(--fg);font:14px/1.4 Georgia,serif}
header{position:sticky;top:0;background:var(--card);border-bottom:1px solid var(--line);padding:8px 16px;z-index:2;display:flex;gap:12px;flex-wrap:wrap;align-items:center}
main{padding:12px 16px;max-width:1400px}
h2{font-size:17px;margin:18px 0 6px}
.card{background:var(--card);border:1px solid var(--line);border-radius:6px;padding:8px;margin-bottom:12px;overflow-x:auto}
canvas{image-rendering:pixelated;display:block}
.note{font-size:12px;color:#6b6152}
select,button{font:inherit}
</style></head><body>
<header><b>Baked match sprites</b>
<label>Kit <select id="kit"></select></label>
<label>Scale <select id="scale"><option value="2" selected>2x (10 px/m)</option><option value="1">1x (5 px/m)</option></select></label>
<label>Zoom <select id="zoom"><option>2</option><option selected>3</option><option>4</option><option>6</option></select></label>
<label><input type="checkbox" id="mud"> mud overlay</label>
<button id="pause">pause</button><span class="note" id="meta"></span></header>
<main>
<h2>Scene: side-on strip (V3), two kits, ball</h2><div class="card"><canvas id="scene"></canvas><div class="note">Pick the second kit: <select id="kit2"></select></div></div>
<h2>Players: every animation x 8 directions</h2><div class="card"><canvas id="players"></canvas></div>
<h2>Keeper (G2: same kit + cap)</h2><div class="card"><canvas id="keeper"></canvas></div>
<h2>Officials (O2 hand grids)</h2><div class="card"><canvas id="officials"></canvas></div>
<h2>Touchline people (doll)</h2><div class="card"><canvas id="people"></canvas></div>
<h2>Crowd (CR1)</h2><div class="card"><canvas id="crowd"></canvas></div>
<h2>Ball (BA2)</h2><div class="card"><canvas id="ball"></canvas></div>
<h2>Touchline props (T1)</h2><div class="card"><canvas id="props"></canvas></div>
</main>
<script>
const IDX=__INDEX__;const PNG=__PNGS__;
const IMG={};let loaded=0;const files=Object.keys(PNG);
files.forEach(f=>{const i=new Image();i.onload=()=>{if(++loaded===files.length)start();};i.src='data:image/png;base64,'+PNG[f];IMG[f]=i;});
const $=id=>document.getElementById(id);const DIRS=IDX.directions;let T=0,PAUSED=false;
const sheet=(kind,scale,club)=>IDX.sheets.find(s=>s.kind===kind&&(scale==null||s.scale===scale)&&(!club||s.club===club));
function draw(ctx,sh,name,x,y,overlay){const L=IDX.layouts[sh.layout];const r=L.regions[name];if(!r)return;ctx.drawImage(IMG[sh.file],r[0],r[1],r[2],r[3],Math.round(x-r[4]),Math.round(y-r[5]),r[2],r[3]);
 if(overlay){ctx.drawImage(IMG[overlay.file],r[0],r[1],r[2],r[3],Math.round(x-r[4]),Math.round(y-r[5]),r[2],r[3]);}}
function frame(L,anim){const a=L.animations[anim];const f=Math.floor(T*a.fps);return a.loop?f%a.frames:Math.min(a.frames-1,f%(a.frames+3));}
function label(ctx,s,x,y){ctx.fillStyle='#3a332b';ctx.font='7px monospace';ctx.fillText(s,x,y);}
function setup(c,w,h){const z=+$('zoom').value;c.width=w;c.height=h;c.style.width=(w*z)+'px';const ctx=c.getContext('2d');ctx.imageSmoothingEnabled=false;return ctx;}
function start(){
 const kits=[...new Set(IDX.sheets.filter(s=>s.kind==='kit').map(s=>s.club))];
 for(const sel of [$('kit'),$('kit2')])kits.forEach(k=>{const s=IDX.sheets.find(x=>x.club===k);const o=document.createElement('option');o.value=k;o.textContent=k+(s.uncertain?' (uncertain)':'');sel.appendChild(o);});
 $('kit').value='blackburn-rovers';$('kit2').value='wolverhampton-wanderers';
 $('pause').onclick=()=>{PAUSED=!PAUSED;$('pause').textContent=PAUSED?'play':'pause';};
 let last=performance.now();(function loop(now){if(!PAUSED)T+=(now-last)/1000;last=now;render();requestAnimationFrame(loop);})(last);}
function grid(id,sh,actor,anims,dirs,bg){const L=IDX.layouts[sh.layout];const r0=Object.values(L.regions)[0];const cw=r0[2]+4,ch=r0[3]+4;
 const c=$(id);const ctx=setup(c,46+dirs.length*cw,10+anims.length*ch);ctx.fillStyle=bg||'#b9c49a';ctx.fillRect(0,0,c.width,c.height);
 dirs.forEach((d,i)=>label(ctx,d,48+i*cw,8));const mud=$('mud').checked&&sh.kind==='kit'?sheet('overlay',sh.scale):null;
 anims.forEach((an,j)=>{label(ctx,an.split('/').pop(),2,14+j*ch+ch/2);const f=frame(L,an);dirs.forEach((d,i)=>draw(ctx,sh,an+'/'+d+'/'+f,46+i*cw+r0[4]+2,10+j*ch+r0[5]+2,mud));});}
function render(){const sc=+$('scale').value;const kit=sheet('kit',sc,$('kit').value);const kit2=sheet('kit',sc,$('kit2').value);
 const L=IDX.layouts[kit.layout];$('meta').textContent=kit.file+' '+L.size.join('x');
 const pa=Object.keys(L.animations).filter(a=>a.startsWith('player/')),ka=Object.keys(L.animations).filter(a=>a.startsWith('keeper/'));
 grid('players',kit,'player',pa,DIRS);grid('keeper',kit,'keeper',ka,DIRS);
 const os=sheet('officials',sc);grid('officials',os,'',Object.keys(IDX.layouts[os.layout].animations),['S','E','N','W']);
 const ps=sheet('people',sc);grid('people',ps,'',Object.keys(IDX.layouts[ps.layout].animations),DIRS);
 // crowd
 {const cs=sheet('crowd',1);const CL=IDX.layouts[cs.layout];const c=$('crowd');const moods=['calm','cheer','angry','encroach'];const ctx=setup(c,4*84,70);ctx.fillStyle='#8c8064';ctx.fillRect(0,0,c.width,c.height);
  moods.forEach((m,mi)=>{label(ctx,m,mi*84+2,8);for(let row=0;row<3;row++)for(let k=0;k<12;k++){const v=(k+row*5)%12;const an='crowd/v'+String(v).padStart(2,'0')+'/'+m;const a=CL.animations[an];const f=Math.floor(T*a.fps+k*0.37+row)%a.frames;draw(ctx,cs,an+'/'+f,mi*84+6+k*6+(row%2)*3,30+row*14+(m==='encroach'&&row===2?Math.round((Math.sin(T*0.6+k)+1)*3):0));}});}
 // ball
 {const bs=sheet('ball',sc);const BL=IDX.layouts[bs.layout];const c=$('ball');const ctx=setup(c,240,26*sc);ctx.fillStyle='#5c7842';ctx.fillRect(0,0,c.width,c.height);
  ['dry','damp','soaked','muddy'].forEach((w,i)=>{label(ctx,w,i*60+2,8);const f=Math.floor(T*12)%8;draw(ctx,bs,'ball/ground/'+w+'/'+f,i*60+14,22*sc);draw(ctx,bs,'ball/high/'+w+'/'+f,i*60+34,22*sc);});}
 // props
 {const ts=sheet('touchline',1);const TL=IDX.layouts[ts.layout];const c=$('props');const ctx=setup(c,360,70);ctx.fillStyle='#5c7842';ctx.fillRect(0,0,c.width,c.height);ctx.fillStyle='#9aa6ac';ctx.fillRect(0,0,c.width,22);
  const f2=Math.floor(T*2)%2;draw(ctx,ts,'prop/pavilion/'+f2,40,52);draw(ctx,ts,'prop/shed/0',100,52);draw(ctx,ts,'prop/tent/'+f2,140,52);draw(ctx,ts,'prop/carriage/'+f2,190,52);draw(ctx,ts,'prop/telegraph-pole/0',300,52);draw(ctx,ts,'prop/press-table/0',280,58);
  for(let x=0;x<360;x+=16)draw(ctx,ts,'prop/rope/0',x,64);for(let x=220;x<260;x+=12)draw(ctx,ts,'prop/railings/0',x,57);[230,234,238].forEach(x=>draw(ctx,ts,'prop/chair/0',x,62));draw(ctx,ts,'prop/bucket/0',262,62);
  const pl=sheet('people',1);[['committee',232],['committee-b',236],['press',280],['press',286]].forEach(([n,x])=>draw(ctx,pl,n+'/idle/S/'+frame(IDX.layouts[pl.layout],n+'/idle'),x,63));
  const cx=(T*8)%380-10;draw(ctx,pl,'constable/walk/E/'+frame(IDX.layouts[pl.layout],'constable/walk'),cx,66);}
 // scene
 {const c=$('scene');const W=360,H=120;const ctx=setup(c,W*sc,H*sc);c.style.width=(W*+$('zoom').value)+'px';ctx.save();ctx.scale(sc,sc);ctx.fillStyle='#9aa6ac';ctx.fillRect(0,0,W,24);for(let y=24;y<H;y++){ctx.fillStyle=(y>>2)%2?'#5c7842':'#64804a';ctx.fillRect(0,y,W,1);}ctx.fillStyle='#e8e4d6';ctx.fillRect(0,32,W,1);ctx.fillRect(0,112,W,1);ctx.fillRect(180,32,1,80);
  const crowd=sheet('crowd',1);for(let k=0;k<58;k++){const v=k%12;draw(ctx,crowd,'crowd/v'+String(v).padStart(2,'0')+'/calm/'+(Math.floor(T+k*0.3)%2),4+k*6,24);}ctx.restore();
  const ents=[];for(let i=0;i<10;i++){const sh=i%2?kit2:kit;const ph=i*1.7;const x=180+Math.sin(T*0.35+ph)*150,z=72+Math.sin(T*0.5+ph*1.3)*32;const vx=Math.cos(T*0.35+ph)*0.35*150,vz=Math.cos(T*0.5+ph*1.3)*0.5*32;
   const ang=Math.atan2(vx,vz);const d=((Math.round(ang/(Math.PI/4))%8)+8)%8;ents.push({sh,x,z,d,i});}
  const ks=IDX.layouts[kit.layout];ents.push({sh:kit,x:20,z:72+Math.sin(T*0.7)*8,d:2,gk:1});ents.push({sh:kit2,x:340,z:72+Math.cos(T*0.6)*8,d:6,gk:1});
  const os=sheet('officials',sc),ol=IDX.layouts[os.layout];
  ents.sort((a,b)=>a.z-b.z);ctx.save();
  for(const e of ents){const an=e.gk?'keeper/idle':'player/run';const f=frame(ks,an);ctx.fillStyle='rgba(20,22,30,.3)';ctx.fillRect((e.x-3)*sc,(e.z)*sc,7*sc,1*sc);draw(ctx,e.sh,an+'/'+DIRS[e.d]+'/'+f,e.x*sc,e.z*sc,$('mud').checked?sheet('overlay',sc):null);}
  draw(ctx,os,'umpire-a/'+((T%6)>4.5?'flag':'walk')+'/E/'+frame(ol,(T%6)>4.5?'umpire-a/flag':'umpire-a/walk'),90*sc,50*sc);
  draw(ctx,os,'referee/walk/'+(Math.cos(T*0.2)>0?'E':'W')+'/'+frame(ol,'referee/walk'),(180+Math.sin(T*0.2)*100)*sc,117*sc);
  const bs=sheet('ball',sc);const bx=180+Math.sin(T*0.9)*120,bz=80,bh=Math.abs(Math.sin(T*1.8))*14;ctx.fillStyle='rgba(20,22,30,.35)';ctx.fillRect((bx-2)*sc,bz*sc,4*sc,sc);draw(ctx,bs,'ball/'+(bh>8?'high':'ground')+'/dry/'+(Math.floor(T*14)%8),bx*sc,(bz-bh)*sc);
  ctx.restore();}
}
</script></body></html>
"""


def main(out: str) -> None:
    idx = json.loads((m.OUT_DIR / "index.json").read_text(encoding="utf-8"))
    pngs = {s["file"]: base64.b64encode((m.OUT_DIR / s["file"]).read_bytes()).decode("ascii") for s in idx["sheets"]}
    html = PAGE.replace("__INDEX__", json.dumps(idx)).replace("__PNGS__", json.dumps(pngs))
    Path(out).write_text(html, encoding="utf-8")
    print(f"wrote {out} ({len(html) // 1024} KB)")


if __name__ == "__main__":
    main(sys.argv[1])
