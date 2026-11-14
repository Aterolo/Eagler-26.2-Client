#!/usr/bin/env node
"use strict";
/* Inject Aterix's browser-native game-canvas recorder into standalone HTML. */
const fs = require("node:fs");
const [, , input, output] = process.argv;
if (!input || !output) { console.error("usage: node inject_screen_recorder.js <in.html> <out.html>"); process.exit(2); }
let html = fs.readFileSync(input, "utf8");
const marker = "ATERIX_SCREEN_RECORDER_V1";
if (html.includes(marker)) { fs.writeFileSync(output, html); process.exit(0); }
const script = String.raw`<script id="aterix-screen-recorder">
/* ATERIX_SCREEN_RECORDER_V1 */
(() => {
  if (window.__aterixScreenRecorder) return;
  window.__aterixScreenRecorder = true;
  const KEY_VISIBLE = "aterix_rec_indicator_visible";
  const KEY_POSITION = "aterix_rec_indicator_position";
  let recorder = null, stream = null, chunks = [], startedAt = 0, elapsedTimer = null;
  let visible = true, dragging = false, moved = false, dx = 0, dy = 0;
  try { visible = localStorage.getItem(KEY_VISIBLE) !== "false"; } catch (_) {}
  const style = document.createElement("style");
  style.textContent = "#aterix-recorder{position:fixed;z-index:2147483647;top:12px;right:12px;display:flex;align-items:center;gap:8px;padding:7px 11px;border:1px solid #7042a5;border-radius:9px;background:rgba(13,9,22,.94);color:#eee5ff;font:600 12px/1.2 Arial,sans-serif;box-shadow:0 4px 18px #0008;cursor:grab;user-select:none;touch-action:none}#aterix-recorder button{border:0;border-radius:6px;padding:6px 9px;background:#7540b5;color:white;font-weight:700;cursor:pointer}#aterix-recorder button:hover{background:#9058d4}#aterix-recorder .rec-dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:#777;margin-right:5px}#aterix-recorder.recording .rec-dot{background:#ff334f;box-shadow:0 0 8px #ff334f}#aterix-recorder .rec-label{white-space:nowrap}#aterix-recorder[hidden]{display:none!important}";
  (document.head || document.documentElement).appendChild(style);
  const panel = document.createElement("div");
  panel.id = "aterix-recorder";
  panel.setAttribute("role", "status");
  panel.innerHTML = '<span class="rec-label"><span class="rec-dot"></span><span id="aterix-rec-time">REC · F8</span></span><button id="aterix-rec-toggle" type="button">Start</button>';
  (document.body || document.documentElement).appendChild(panel);
  const label = panel.querySelector("#aterix-rec-time");
  const button = panel.querySelector("#aterix-rec-toggle");
  function loadPosition() {
    try {
      const p = JSON.parse(localStorage.getItem(KEY_POSITION) || "null");
      if (p && Number.isFinite(p.left) && Number.isFinite(p.top)) {
        panel.style.left = Math.max(0, Math.min(innerWidth - panel.offsetWidth, p.left)) + "px";
        panel.style.top = Math.max(0, Math.min(innerHeight - panel.offsetHeight, p.top)) + "px";
        panel.style.right = "auto";
      }
    } catch (_) {}
  }
  function savePosition() {
    try { const r = panel.getBoundingClientRect(); localStorage.setItem(KEY_POSITION, JSON.stringify({left:r.left,top:r.top})); } catch (_) {}
  }
  function setVisible(v) {
    visible = !!v; panel.hidden = !visible;
    try { localStorage.setItem(KEY_VISIBLE, String(visible)); } catch (_) {}
  }
  function elapsed(ms) {
    const n = Math.floor(ms/1000), h = Math.floor(n/3600), m = Math.floor(n%3600/60), s = n%60;
    return (h ? String(h).padStart(2,"0")+":" : "") + String(m).padStart(2,"0")+":"+String(s).padStart(2,"0");
  }
  function refresh() {
    const active = !!recorder && recorder.state === "recording";
    panel.classList.toggle("recording", active);
    button.textContent = active ? "Stop" : "Start";
    label.textContent = active ? elapsed(Date.now()-startedAt) : "REC · F8";
  }
  function gameCanvas() {
    const list = Array.from(document.querySelectorAll("canvas")).filter(c => c.width>0 && c.height>0);
    return list.find(c => c.id === "game_frame") || list.sort((a,b)=>b.width*b.height-a.width*a.height)[0] || null;
  }
  function start() {
    if (!window.MediaRecorder) { label.textContent = "Recorder unsupported"; return; }
    const canvas = gameCanvas();
    if (!canvas || typeof canvas.captureStream !== "function") { label.textContent = "Game canvas unavailable"; return; }
    try {
      stream = canvas.captureStream(30);
      const types = ["video/webm;codecs=vp9","video/webm;codecs=vp8","video/webm"];
      const mime = types.find(t => MediaRecorder.isTypeSupported(t));
      recorder = mime ? new MediaRecorder(stream,{mimeType:mime}) : new MediaRecorder(stream);
      chunks = [];
      recorder.ondataavailable = e => { if (e.data && e.data.size) chunks.push(e.data); };
      recorder.onerror = e => { console.error("[Aterix Recorder]",e.error||e); label.textContent="Recording error"; };
      recorder.onstop = () => {
        const blob = new Blob(chunks,{type:(recorder&&recorder.mimeType)||"video/webm"});
        chunks = [];
        if (blob.size) {
          const url = URL.createObjectURL(blob), a = document.createElement("a");
          a.href=url; a.download="aterix-recording-"+new Date().toISOString().replace(/[:.]/g,"-")+".webm";
          a.style.display="none"; document.body.appendChild(a); a.click(); a.remove();
          setTimeout(()=>URL.revokeObjectURL(url),60000);
        }
        if (stream) stream.getTracks().forEach(t=>t.stop());
        stream=null; recorder=null;
        if (elapsedTimer) clearInterval(elapsedTimer);
        elapsedTimer=null; refresh();
      };
      startedAt=Date.now(); recorder.start(1000); elapsedTimer=setInterval(refresh,250); refresh();
    } catch(e) {
      console.error("[Aterix Recorder] Start failed:",e); label.textContent="Start failed";
      if(stream) stream.getTracks().forEach(t=>t.stop());
      stream=null; recorder=null;
    }
  }
  function stop() { if(recorder && recorder.state!=="inactive") recorder.stop(); }
  function toggle() { if(recorder && recorder.state==="recording") stop(); else start(); }
  button.addEventListener("click",e=>{e.stopPropagation();toggle();});
  panel.addEventListener("pointerdown",e=>{
    if(e.target===button) return;
    dragging=true; moved=false;
    const r=panel.getBoundingClientRect(); dx=e.clientX-r.left; dy=e.clientY-r.top;
    panel.setPointerCapture(e.pointerId); panel.style.right="auto";
    panel.style.left=r.left+"px"; panel.style.top=r.top+"px"; panel.style.cursor="grabbing";
  });
  panel.addEventListener("pointermove",e=>{
    if(!dragging)return; moved=true;
    panel.style.left=Math.max(0,Math.min(innerWidth-panel.offsetWidth,e.clientX-dx))+"px";
    panel.style.top=Math.max(0,Math.min(innerHeight-panel.offsetHeight,e.clientY-dy))+"px";
  });
  function endDrag(){if(!dragging)return;dragging=false;panel.style.cursor="grab";if(moved)savePosition();}
  panel.addEventListener("pointerup",endDrag); panel.addEventListener("pointercancel",endDrag);
  window.addEventListener("keydown",e=>{
    if(e.code==="F8"&&!e.repeat){e.preventDefault();e.stopPropagation();toggle();}
    else if(e.code==="F9"&&!e.repeat){e.preventDefault();e.stopPropagation();setVisible(!visible);}
  },true);
  window.addEventListener("resize",()=>{
    const r=panel.getBoundingClientRect();
    if(r.left+panel.offsetWidth>innerWidth)panel.style.left=Math.max(0,innerWidth-panel.offsetWidth)+"px";
    if(r.top+panel.offsetHeight>innerHeight)panel.style.top=Math.max(0,innerHeight-panel.offsetHeight)+"px";
    if(panel.style.left)savePosition();
  });
  loadPosition(); setVisible(visible); refresh();
  console.info("[Aterix Recorder] Ready: F8 start/stop, F9 show/hide indicator.");
})();
</script>`;
const at = html.toLowerCase().lastIndexOf("</body>");
if (at >= 0) html = html.slice(0,at) + script + "\n" + html.slice(at);
else {
  const end = html.toLowerCase().lastIndexOf("</html>");
  if (end < 0) throw new Error("No body/html closing tag found");
  html = html.slice(0,end) + script + "\n" + html.slice(end);
}
if (!html.includes(marker)) throw new Error("Recorder injection verification failed");
fs.writeFileSync(output,html);
console.log("[aterix-recorder] Injected F8 WebM recorder and draggable F9 indicator.");
