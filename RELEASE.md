# Publishing Darken releases

## What stays out of git (on purpose)

- `local.properties` — SDK path on your machine  
- `*.jks` / `*.keystore` — signing keys  
- `app/release/` — built release APK (see below)

## Release APK location

After a signed release build, the installable file is typically:

`app/release/app-release.apk`

This folder is **gitignored** so the APK is not pushed with source code.

## Upload APK to GitHub (when repo is online)

1. Create the repository: https://github.com/lokomaster1/darken  
2. Push tags, e.g. `v1.1` (see README).  
3. On GitHub: **Releases → Draft a new release**  
   - Tag: `v1.1`  
   - Title: `Darken 1.1.0`  
   - Attach `app-release.apk` (or rename to `darken-1.1.0.apk`)  
4. Publish the release.

### Command line (optional, requires `gh` login)

```bash
gh release create v1.1 app/release/app-release.apk \
  --title "Darken 1.1.0" \
  --notes "Screen dimming overlay — GPL-3.0, no analytics, no Internet."
```

## Before each release

1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`  
2. Build signed APK in Android Studio (or your signing setup)  
3. Test install on a device  
4. Commit source, tag `vX.Y`, push, then attach APK to GitHub Release only
