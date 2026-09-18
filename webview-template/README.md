# AppForge WebView Template

A real, clean-room native Android WebView app template - drawer navigation, bottom
navigation, and file upload/download all driven by a single injected JSON config,
so this template serves every generated app rather than needing a fork per app.

This is the paid-tier "source code export" / native path, complementing the
existing Bubblewrap/TWA pipeline (free tier). Both pipelines can coexist:
Bubblewrap for fast, Chrome-dependent TWA apps; this template for apps that need
full native control, offline shell behavior, or a real Kotlin project the user
can download and keep extending themselves.

## What's customizable per generated app

Everything is driven by app/src/main/assets/app_config.json, written by CI at
build time from the app's row in Supabase (apps + app_settings).

- Navigation drawer items (navigation.drawerItems)
- Bottom nav items, up to 5 (navigation.bottomNavItems)
- Primary/secondary color, dark mode (theme.*)
- File upload, camera capture, file download (features.*)
- Pull to refresh, share button, WhatsApp button (features.*)

See app/src/main/assets/app_config.json for a complete example.

## Why file upload/download needed real code

A bare android.webkit.WebView does NOT support <input type="file"> or
triggering downloads:

- Upload: WebChromeClient.onShowFileChooser() launches a system file/camera
  picker and hands the result back to the page. See WebAppFragment.kt.
- Download: WebView.setDownloadListener() catches the download request and
  hands it to Android's DownloadManager. Also in WebAppFragment.kt.

## How this plugs into the existing build pipeline

.github/workflows/build-webview.yml mirrors the existing repository_dispatch
build -> sign -> upload -> callback pattern, with these steps swapped:

1. Instead of bubblewrap init, the workflow writes app_config.json directly
   from the dispatch payload.
2. Instead of bubblewrap build, it runs a normal ./gradlew assembleRelease
   bundleRelease.
3. Signing, artifact verification (non-empty APK/AAB check), and the callback
   step follow the same per-app keystore pattern. The callback secret is read
   from secrets.CALLBACK_SECRET, not from client_payload.

## Local development

./gradlew assembleDebug

Edit app/src/main/assets/app_config.json directly to preview different
drawer/bottom-nav/theme configurations without needing the full CI pipeline.

## Next steps to make this production-ready

- Wire the WhatsApp button and share button (both are in FeatureFlags but not
  yet rendered in the toolbar)
- Push notification support (FCM)
- Icon generation: launcher icon (mipmap/ic_launcher*) needs to be generated
  per app by the wizard's icon designer
- Splash screen: wire theme.splashType to Android 12+'s SplashScreen API
