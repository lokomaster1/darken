# Darken release guide

Download signed APKs from [GitHub Releases](https://github.com/lokomaster1/darken/releases). See [CHANGELOG.md](CHANGELOG.md) for changes in each version.

## Published APKs

The latest published version is **1.3.1**. See the [1.3.1 changelog](CHANGELOG.md#131).

- [v1.3.1](https://github.com/lokomaster1/darken/releases/tag/v1.3.1) — `darken-1.3.1.apk`, version code 16
- [v1.3.0](https://github.com/lokomaster1/darken/releases/tag/v1.3.0) — `darken-1.3.0.apk`, version code 15
- [v1.2.7](https://github.com/lokomaster1/darken/releases/tag/v1.2.7) — `Darken-1.2.7.apk`, version code 14
- [v1.2.6](https://github.com/lokomaster1/darken/releases/tag/v1.2.6) — `Darken-1.2.6.apk`, version code 13
- [v1.2.5](https://github.com/lokomaster1/darken/releases/tag/v1.2.5) — `darken-1.2.5.apk`, version code 12
- [v1.2.2](https://github.com/lokomaster1/darken/releases/tag/v1.2.2) — `darken-1.2.2.apk`, version code 9
- [v1.1.1](https://github.com/lokomaster1/darken/releases/tag/v1.1.1) — `darken-1.1.1.apk`, version code 6

Tags `v1.0`, `v1.0.1`, `v1.0.2`, `v1.1`, `v1.2.3`, and `v1.2.4` provide source snapshots through the [tag archive](https://github.com/lokomaster1/darken/tags).

## Prepare a release

1. Set the next `versionName` and increase `versionCode` in [app/build.gradle.kts](app/build.gradle.kts). Use the version name for the tag (`vX.Y.Z`) and APK asset (`darken-X.Y.Z.apk`).
2. Update **Published APKs** in the release commit with the version, version code, tag link, and APK filename. Add the release notes to [CHANGELOG.md](CHANGELOG.md) and a [F-Droid changelog](fastlane/metadata/android/en-US/changelogs) file named after the new version code.
3. Build a signed **release** APK with Android Studio’s signing wizard or an equivalent local signing setup. Keep the signing key and its credentials outside the repository. Local SDK configuration and build output in `app/release/` and `app/build/` are ignored by Git.
4. Check the APK with `apksigner verify --print-certs` and compare the signing certificate with the GitHub release fingerprint in [README.md](README.md#certificate-fingerprint-exclude-f-droid-version). Test both a clean install and an update over the previous GitHub APK.
5. Tag the source used to build the APK. Once the tag is on GitHub, draft a release for that tag, title it `Darken X.Y.Z`, attach the signed APK, and use the matching changelog section as the description. Review the draft before publishing.

To create the draft with GitHub CLI, replace `X.Y.Z` with the release version and point the paths at the renamed APK and the prepared release notes.

```bash
version=X.Y.Z
gh release create "v${version}" "/path/to/darken-${version}.apk" \
  --repo lokomaster1/darken \
  --verify-tag \
  --draft \
  --title "Darken ${version}" \
  --notes-file /path/to/release-notes.md
```
