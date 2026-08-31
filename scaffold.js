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

  if (!twaJson.packageId || !twaJson.host) {
    throw new Error('TWA manifest must include packageId and host')
  }

  // AppForge originally emitted appVersionName while Bubblewrap consumes appVersion.
  // Normalize both shapes and make versionCode monotonically increase with Actions runs.
  const runNumber = Number.parseInt(process.env.GITHUB_RUN_NUMBER || '', 10)
  const versionCode = Number.isFinite(runNumber) && runNumber > 0
    ? runNumber
    : (Number.parseInt(twaJson.appVersionCode, 10) || 1)
  twaJson.appVersionCode = versionCode
  twaJson.appVersion = twaJson.appVersion || twaJson.appVersionName || `1.0.${versionCode}`
  delete twaJson.appVersionName

  console.log(
    'Manifest received. packageId =', twaJson.packageId,
    'host =', twaJson.host,
    'versionCode =', twaJson.appVersionCode,
    'version =', twaJson.appVersion,
  )

  const tmpPath = '/tmp/appforge-twa-manifest.json'
  fs.writeFileSync(tmpPath, JSON.stringify(twaJson, null, 2))

  const manifest = await TwaManifest.fromFile(tmpPath)
  const validationError = manifest.validate()
  if (validationError) throw new Error(`Invalid TWA manifest: ${validationError}`)

  fs.mkdirSync(projectDir, { recursive: true })

  const gen = new TwaGenerator()
  await gen.createTwaProject(projectDir, manifest, new ConsoleLog('appforge'))
  // Persist a copy inside project for `bubblewrap build` later.
  fs.copyFileSync(tmpPath, path.join(projectDir, 'twa-manifest.json'))
  console.log('Scaffold ready at', projectDir)
}

main().catch((e) => {
  console.error('SCAFFOLD ERROR:', e && (e.stack || e.message || e))
  process.exit(1)
})
