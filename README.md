# Afterlight

Afterlight is a local-first Android reader companion for saving clean text and Markdown to read later.

It is intentionally **not** a browser, feed reader, source manager, or replacement for `paywall-reader`.

## Product boundary

- `paywall-reader`: browse/open sources and resolve reading flows in a WebView.
- `Afterlight`: receive URL/text/HTML, extract readable content locally, save Markdown in app storage, and render it in a native distraction-free reader.

## MVP features

- Android share target for `text/plain` and `text/html`.
- Private companion intent: `com.juani.afterlight.SAVE_ARTICLE`.
- Extras supported: `title`, `source`, `originalUrl`, `resolvedUrl`, `html`, `text`.
- Room metadata database.
- Markdown files stored locally in the app sandbox with YAML frontmatter.
- Native Jetpack Compose inbox and reader.
- Night mode, font-size control, and Markdown export.
- Local HTML extraction using a bundled readability-style Jsoup extractor.

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
./gradlew testDebugUnitTest assembleDebug --no-daemon
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```
