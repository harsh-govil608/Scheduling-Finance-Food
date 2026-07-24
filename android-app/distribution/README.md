# Releasing an update to pilot testers

This app has no Play Store listing, so it can't use Play's built-in update mechanism. Instead,
`UpdateChecker.kt` (in the app) checks `latest.json` in this folder - hosted for free via GitHub's
raw-content URLs, since the code is already in this repo - and offers a one-tap download+install
if the committed `versionCode` is higher than what's installed.

**This only works for testers already running a build that contains `UpdateChecker.kt`** (v0.4.0
and later). Anyone on an older build still needs one final manually-sent APK to get onto the
updater - after that, every future release should reach them automatically.

## Steps for every release after a real code change

1. Bump `versionCode` and `versionName` in `android-app/app/build.gradle.kts`.
2. `gradle assembleDebug` from `android-app/`.
3. Copy the resulting APK to `android-app/distribution/app-latest.apk`, overwriting the old one.
4. Update `android-app/distribution/latest.json`:
   - `versionCode` / `versionName` must match step 1 exactly, or the updater will never fire.
   - `downloadUrl` stays the same (`.../main/android-app/distribution/app-latest.apk`) - it's the
     file *content* that changes, not the URL.
   - `notes` - one or two plain-language lines a non-technical tester should see before tapping
     install. Not a full changelog.
5. Commit both the new `app-latest.apk` and `latest.json` together, and push to `main`.

Testers will see the update banner on their next Home open with a live internet connection - it
fails silently (no error shown) if they're offline or GitHub is unreachable, and just retries
next time.

## Why commit a binary APK to git

The alternative (GitHub Releases) needs the `gh` CLI or a personal access token, neither of which
was set up when this was built. Committing the APK directly works today with zero new
tools/accounts, at the cost of the repo's history growing by roughly one APK's size (currently
~16 MB) per release. Worth revisiting once there are more than a couple of testers, or once a
real Play Store listing exists and can replace this whole mechanism.
