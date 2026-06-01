# Darken

Free, open-source Android app that dims the screen with a full-screen overlay. No ads, no analytics, no Internet permission.

Why this exists:

I couldn’t really find a modern, open-source Android app that does this properly — something up-to-date, simple, and with all the basic features easily accessible. So I built this for myself as a clean, minimal tool that just does its job without unnecessary complexity.


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

## Certificate fingerprint:

SHA256: 19:7F:C8:BF:5A:D8:A8:E9:D0:76:CD:5B:DB:7C:9F:BA:98:32:42:01:BD:8A:5E:30:FC:FD:ED:93:15:F0:85:D7



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

## Known limitations

There are no specific known issues, but due to Android and OEM differences, behavior may vary across devices (especially Samsung UI / HyperOS / heavily customized systems). If you encounter anything unexpected, feedback is welcome — it helps improve compatibility.


Security-sensitive apps
Some security-sensitive apps (such as banking apps, password managers, or authentication/2FA apps) may restrict or block interaction when an overlay is active. This is a security measure on their side to prevent potential screen overlay attacks.
If you experience unexpected behavior in such apps, try disabling the overlay.


## Third-party libraries

AndroidX / Jetpack Compose / DataStore (Apache License 2.0), linked into the app binary.


## Developer note

Code was written with assistance from **[Cursor](https://cursor.com)** (AI-assisted IDE). Thank you to the Cursor team and the models behind it


## More info
- **Repository:** https://github.com/lokomaster1/darken  
- **Contact:** diskus.barge163@simplelogin (dot) com  
- **License:** [GNU GPL v3.0 or later](LICENSE) — Copyright (C) 2026 lokomaster1

---

## Česky

**Darken** je svobodná aplikace pro Android, která dodatečně ztmaví displej překryvnou vrstvou. Bez reklam, bez analytiky, bez oprávnění k internetu.

- Repozitář: https://github.com/lokomaster1/darken  
- Kontakt: diskus.barge163@simplelogin (dot) com  
- Licence: GNU GPL v3.0+, autor **lokomaster1**

Sestavení: `./gradlew assembleDebug`  
Soukromí: [PRIVACY.md](PRIVACY.md)  
APK na GitHub: viz [RELEASE.md](RELEASE.md)

**Vývoj:** Kód vznikal s pomocí [Cursor](https://cursor.com) (IDE s AI). Děkuji tvůrcům Cursoru.
