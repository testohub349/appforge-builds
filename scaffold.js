#!/usr/bin/env node
/**
 * Non-interactive TWA scaffold - replaces `bubblewrap init`.
 * Reads a TWA manifest from a URL and generates a signed-ready Android project.
 *
 * Usage: node scaffold.js <twa_manifest_url> <project_dir>
 */
const fs = require('fs')
const path = require('path')
const { TwaGenerator, TwaManifest, ConsoleLog } = require('@bubblewrap/core')

async function main() {
  const [, , twaManifestUrl, projectDir] = process.argv
  if (!twaManifestUrl || !projectDir) {
    console.error('Usage: scaffold.js <twa_manifest_url> <project_dir>')
    process.exit(1)
  }

  console.log(`Fetching TWA manifest from ${twaManifestUrl}`)
  const res = await fetch(twaManifestUrl)
  if (!res.ok) throw new Error(`HTTP ${res.status} fetching manifest`)
  const twaJson = await res.json()
  console.log('Manifest received. packageId =', twaJson.packageId, 'host =', twaJson.host)

  const tmpPath = '/tmp/appforge-twa-manifest.json'
  fs.writeFileSync(tmpPath, JSON.stringify(twaJson, null, 2))

  const manifest = await TwaManifest.fromFile(tmpPath)
  fs.mkdirSync(projectDir, { recursive: true })

  const gen = new TwaGenerator()
  await gen.createTwaProject(projectDir, manifest, new ConsoleLog('appforge'))
  // Persist a copy inside project for `bubblewrap build` later
  fs.copyFileSync(tmpPath, path.join(projectDir, 'twa-manifest.json'))
  console.log('Scaffold ready at', projectDir)
}

main().catch((e) => {
  console.error('SCAFFOLD ERROR:', e && (e.stack || e.message || e))
  process.exit(1)
})
