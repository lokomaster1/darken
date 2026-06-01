# Publishing Darken releases

## What stays out of git (on purpose)

- `local.properties` — SDK path on your machine  
- `*.jks` / `*.keystore` — signing keys  
- `app/release/` — built release APK (see below)

## Release APK location

After a signed release build, the installable file is typically:

`app/release/app-release.apk`

This folder is **gitignored** so the APK is not pushed with source code.

## GitHub Releases layout (APK files)

APK binaries are **not** in the git tree (see `.gitignore`). Each version is published as a **GitHub Release** attached to a tag:

| Git tag   | Suggested APK asset name   | Source version |
|-----------|----------------------------|----------------|
| `v1.1.1`  | `darken-1.1.1.apk`         | `versionName` in `app/build.gradle.kts` at that tag |
| `v1.1.0`  | `darken-1.1.0.apk`         | (older builds, if you still have the APK) |

Older tags (`v1.0`, `v1.0.1`, …) can stay source-only unless you have signed APKs to upload later.

## Upload APK to GitHub

1. Repository: https://github.com/lokomaster1/darken  
2. Push branch and tags (see README).  
3. **Releases → Draft a new release**  
   - Choose tag: `v1.1.1`  
   - Title: `Darken 1.1.1`  
   - Attach APK renamed to `darken-1.1.1.apk` (from `app/release/app-release.apk` on your machine)  
4. Publish.

### Command line (requires [GitHub CLI](https://cli.github.com/) — `gh auth login`)

```bash
cp app/release/app-release.apk /tmp/darken-1.1.1.apk
gh release create v1.1.1 /tmp/darken-1.1.1.apk \
  --repo lokomaster1/darken \
  --title "Darken 1.1.1" \
  --notes "Screen dimming overlay. GPL-3.0. No Internet, no analytics."
```

## Before each release

1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`  
2. Build signed APK in Android Studio (or your signing setup)  
3. Test install on a device  
4. Commit source, tag `vX.Y`, push, then attach APK to GitHub Release only
