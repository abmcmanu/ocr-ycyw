#!/usr/bin/env node
/**
 * Vérifie l'accessibilité WCAG 2.1 AA (US-26).
 * Prérequis : `ng serve` sur http://localhost:4200
 */
import { execSync } from 'node:child_process';

const BASE = process.env.A11Y_BASE ?? 'http://localhost:4200';
const URLS = [
  { name: 'Widget client', url: process.env.A11Y_URL ?? BASE },
  { name: 'Console conseiller', url: process.env.A11Y_ADVISOR_URL ?? `${BASE}/advisor` },
];
const TAGS = 'wcag2a,wcag2aa,wcag21a,wcag21aa';
const root = new URL('..', import.meta.url).pathname;
const chromedriverPath =
  process.env.CHROMEDRIVER_PATH ??
  `${process.env.HOME}/.browser-driver-manager/chromedriver/mac_arm-148.0.7778.178/chromedriver-mac-arm64/chromedriver`;
const chromedriverFlag = `--chromedriver-path "${chromedriverPath}"`;

let failed = false;

for (const { name, url } of URLS) {
  console.log(`\n🔍 Audit axe-core — ${name} (${url})\n`);
  try {
    execSync(
      `npx --yes @axe-core/cli "${url}" --tags ${TAGS} ${chromedriverFlag} --exit`,
      { stdio: 'inherit', cwd: root }
    );
    console.log(`\n✅ ${name} : aucune violation critique\n`);
  } catch {
    console.error(`\n❌ ${name} : violations détectées\n`);
    failed = true;
  }
}

if (failed) {
  console.error('Corrigez les violations avant validation US-26.\n');
  process.exit(1);
}

console.log('✅ Tous les audits WCAG 2.1 AA sont passés.\n');
