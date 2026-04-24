# 📱 Installing HeroBrawl on your Android phone

You have two paths. Pick the one that matches how comfortable you are with developer tooling.

- [Option A · Easiest: download a prebuilt APK from GitHub (no setup)](#option-a--easiest-download-from-github)
- [Option B · Build it yourself (Android Studio, 5-10 minutes of setup)](#option-b--build-it-yourself)
- [FAQ / troubleshooting](#faq--troubleshooting)

---

## Option A · Easiest: download from GitHub

> TL;DR: you never run a command. You just download and tap-install on your phone.

### Every push to `main` or a PR already builds an APK

1. On your **phone or computer**, open the repo's Actions page:
   `https://github.com/<you>/<repo>/actions` (e.g. [our repo's Actions](../../actions)).
2. Click the most recent green run of **"Android build"**.
3. Scroll to the **Artifacts** section at the bottom and tap **`HeroBrawl-debug-apk`** to download. It's a zip containing `app-debug.apk`.
4. On your phone, extract the zip (Files app → long-press → Extract) and tap the `.apk`.
5. Android will prompt: _"Install unknown apps — allow from this source."_ Tap **Settings → Allow from this source** (just the app you're installing from, e.g. Chrome or Files), then back out and tap **Install**.
6. Open HeroBrawl from your app drawer. Done. ✅

> **Note:** GitHub artifacts from Actions expire after 90 days and require being logged into GitHub to download.

### Even easier: tagged releases

Whenever I (or you) push a tag like `v0.2.1`, the **"Android release"** workflow creates a GitHub Release with the APK attached as a regular asset — no login needed, and it doesn't expire.

1. Open **`https://github.com/<you>/<repo>/releases/latest`** on your phone.
2. Tap **`HeroBrawl-debug.apk`** under **Assets**.
3. Same "allow unknown source + install" prompt as above.

To cut a new release:

```bash
# from any computer with this repo checked out
git tag v0.2.1
git push origin v0.2.1
```

GitHub Actions builds the APK and publishes it automatically.

---

## Option B · Build it yourself

Recommended if you want live debugging / fast iteration.

### B1 · One-time computer setup

1. Install **[Android Studio](https://developer.android.com/studio)** (any recent version; Giraffe or newer). During first run, let it install the Android SDK — that's all you need.
2. Install **[Git](https://git-scm.com/downloads)** if you don't have it yet.
3. Clone the repo:
   ```bash
   git clone https://github.com/<you>/<repo>.git
   cd <repo>
   ```

### B2 · One-time phone setup

Turn on **Developer mode + USB debugging** on your Android phone:

1. Open **Settings → About phone**.
2. Tap **Build number** 7 times. (Yes, really.) You'll see "You are now a developer."
3. Back out to **Settings → System → Developer options** (location varies by OEM — Samsung hides it under "Settings → Developer options"; Pixel is at "System → Developer options").
4. Turn on **USB debugging**.
5. Plug your phone into your computer with a USB cable. Your phone will pop a dialog "Allow USB debugging from this computer?" → tap **Always allow**.

### B3 · Build and run (the easy way)

1. In Android Studio: **File → Open → select the `android/` folder of this repo** (not the repo root).
2. Wait ~60 seconds for Gradle to sync. You'll see `:app` appear in the project tree.
3. Make sure your phone appears in the device dropdown at the top of the window (should show something like `Pixel 7`).
4. Hit the green ▶ **Run 'app'** button. First build takes a minute or two; subsequent builds are fast.
5. The app opens on your phone. You can now iterate — change a Kotlin file, hit ▶ again, and the new build deploys instantly.

### B4 · Build from the command line (if you prefer)

After the one-time setup, from a terminal on your computer:

```bash
cd <repo>/android

# first time only — point at your Android SDK
cp local.properties.template local.properties
# edit local.properties and set sdk.dir to your SDK path, for example:
#   macOS:    sdk.dir=/Users/<you>/Library/Android/sdk
#   Windows:  sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
#   Linux:    sdk.dir=/home/<you>/Android/Sdk

# Build a debug APK
./gradlew :app:assembleDebug

# Install it on a connected phone (USB debugging must be on)
./gradlew :app:installDebug

# Or install a pre-built APK directly with adb
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On **Windows** use `gradlew.bat` instead of `./gradlew`.

`adb` ships with the Android SDK Platform-Tools — Android Studio installs it automatically to `<sdk>/platform-tools/adb` (add that folder to your PATH).

### B5 · Install via wireless adb (no USB cable)

If you're on Android 11+ you can skip the USB cable:

1. On your phone, in **Developer options**, turn on **Wireless debugging**.
2. Tap **Pair device with pairing code**. Your phone shows an IP:PORT and a 6-digit code.
3. On your computer:
   ```bash
   adb pair <phone-ip>:<pair-port>
   # enter the 6-digit code when prompted
   adb connect <phone-ip>:<adb-port>
   ```
4. From now on, `adb devices` lists your phone and `./gradlew :app:installDebug` works wirelessly.

---

## FAQ / troubleshooting

**"Can't install app" / "App not installed"**
Usually means an older version is already installed. Uninstall the old one first (long-press HeroBrawl icon → Uninstall), or pass `-r` to `adb install`.

**"Install blocked" / "For your security, your phone is not allowed to install unknown apps"**
Your phone doesn't trust the app you're installing from. Settings → Apps → (Chrome/Files/whatever) → "Install unknown apps" → Allow.

**"adb: no devices/emulators found"**
- Is USB debugging on? Check Settings → Developer options.
- Accept the "Allow USB debugging" popup on your phone (tap Always allow).
- Try a different USB cable or port. Charge-only cables don't work — you need a data cable.
- On Windows you may need the Google USB Driver from the SDK Manager.

**Gradle build fails: "SDK location not found"**
You skipped `cp local.properties.template local.properties` and setting `sdk.dir`, or Android Studio auto-populated it for a different SDK. Edit `android/local.properties`.

**The app opens but looks empty / crashes immediately**
Look at logs: `adb logcat | grep -i herobrawl`. That'll usually show a Kotlin stack trace. Open an issue with the trace and I'll fix it.

**"I just want the APK, stop making me read instructions"**
Fair. Option A.1 (GitHub Actions artifact) on a fresh push — that's genuinely 4 taps on your phone.

---

## Which APK should I install?

| APK | Signed | Use case |
| --- | --- | --- |
| `HeroBrawl-debug.apk` | Debug keystore (built into every Android SDK) | Easiest to sideload. Use this. |
| `HeroBrawl-release.apk` | Debug keystore (for now — swap for an upload key before Play Store) | Smaller, optimized. Install fine alongside another signing identity. |
| `HeroBrawl-release.aab` | Debug keystore | For uploading to Google Play Console. **You can't install an `.aab` directly on your phone** — Play Store turns it into APKs at install time. |

Install the `debug.apk` on your phone; save the `.aab` for when we're ready for the Play Store.
