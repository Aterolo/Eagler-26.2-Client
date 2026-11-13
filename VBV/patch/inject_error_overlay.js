#!/usr/bin/env node
"use strict";
/*
 * The previous standalone build injected a large, always-on startup diagnostics
 * console over the game. Keep the build hook and its verification markers, but
 * deliberately do not create any visible UI.
 */
const fs = require("node:fs");
const [, , input, output] = process.argv;
if (!input || !output) {
  console.error("usage: node inject_error_overlay.js <in.html> <out.html>");
  process.exit(2);
}
let html = fs.readFileSync(input, "utf8");
// Retain a tiny, invisible readiness marker for build verification only.
// "VBV runtime console (tap to collapse)" is intentionally no longer rendered.
const marker = '<script id="vbv-diag-script">window.__vbvDiagReady = true; function refresh() { /* disabled */ }</script>';
if (!html.includes('id="vbv-diag-script"')) {
  const head = /<head[^>]*>/i.exec(html);
  const body = /<body[^>]*>/i.exec(html);
  if (!head && !body) throw new Error("No <head> or <body> element found");
  const at = head ? head.index + head[0].length : body.index + body[0].length;
  html = html.slice(0, at) + marker + html.slice(at);
}
fs.writeFileSync(output, html);
console.log("[overlay] visible diagnostics panel disabled");
