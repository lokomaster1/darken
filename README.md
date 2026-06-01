# Darken

Free, open-source Android app that dims the screen with a full-screen overlay. No ads, no analytics, no Internet permission.

- **Repository:** https://github.com/lokomaster1/darken  
- **Contact:** diskus.barge163@simplelogin.com  
- **License:** [GNU GPL v3.0 or later](LICENSE) — Copyright (C) 2026 lokomaster1

## Features

- Adjustable dimming intensity (0–99 %)
- Filter colors: gray, warm brown, red, or custom palette / hex
- Czech and English UI
- Settings stored only on device (DataStore)
- Minimal foreground notification while overlay is active (Android 13+)

## Requirements

- Android 12+ (API 31+)
- Permission **Display over other apps** (overlay)
- **Notifications** on Android 13+ (for the service icon)

## Build from source

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Release APK (signed locally): `app/release/app-release.apk` — not committed to git; see [RELEASE.md](RELEASE.md).

## Privacy

See [PRIVACY.md](PRIVACY.md). The in-app privacy policy uses the same content (Settings → Privacy policy).

## Permissions (why)

| Permission | Purpose |
|------------|---------|
| Display over other apps | Draw the dimming overlay |
| Foreground service | Keep overlay running in background |
| Notifications (13+) | Show minimal service notification |
| Ignore battery optimizations | Optional; reduces risk of the system killing the service |

The app does not use the Internet. Overlay permission is powerful; the project is fully open source so behavior can be audited.

## Third-party libraries

AndroidX / Jetpack Compose / DataStore (Apache License 2.0), linked into the app binary.

## Publishing a release APK

Do **not** commit signing keys or `local.properties`. Upload the APK to **GitHub Releases** — steps in [RELEASE.md](RELEASE.md).

## Development note

Most of this codebase was written with assistance from **[Cursor](https://cursor.com)** (AI-assisted IDE). Thank you to the Cursor team and the models behind it — and thank you for checking out Darken.

The author (**lokomaster1**) owns the project direction, testing, and release; you can audit every line in this repository.

---

## Česky

**Darken** je svobodná aplikace pro Android, která dodatečně ztmaví displej překryvnou vrstvou. Bez reklam, bez analytiky, bez oprávnění k internetu.

- Repozitář: https://github.com/lokomaster1/darken  
- Kontakt: diskus.barge163@simplelogin.com  
- Licence: GNU GPL v3.0+, autor **lokomaster1**

Sestavení: `./gradlew assembleDebug`  
Soukromí: [PRIVACY.md](PRIVACY.md)  
APK na GitHub: viz [RELEASE.md](RELEASE.md)

**Vývoj:** většina kódu vznikla s pomocí [Cursor](https://cursor.com) (IDE s AI). Děkuji tvůrcům Cursoru — a děkuji i tobě, že se na projekt díváš.
