# Setup Guide — Build & Install on Your Android Phone

This walks through the one path you actually need right now: getting Bloomodoro built and
running on your own Android phone via Android Studio. iOS/Desktop notes are at the bottom.

## Before you start

- A computer (Windows/macOS/Linux) with **Android Studio** (free — download from
  https://developer.android.com/studio)
- Your Android phone + a USB cable
- ~15–20 minutes for the first build (it downloads a few GB of dependencies)

## Read this first — known gotchas

- **The Gradle wrapper is included** (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`), already pinned to **Gradle 8.9**. You don't need to
  install Gradle separately — `./gradlew ...` or Android Studio's own sync just works.
- **This code has not been compiled by its author** (written in a sandbox without access to
  Google's Maven repository). It's real, idiomatic Kotlin — not pseudocode — but on first
  sync Android Studio may flag a dependency version that's since moved; its quick-fix
  ("update to latest") usually resolves it in one click.
- **Keep the project path simple** — avoid spaces/non-ASCII folder names.
- **First build is slow** — 10–20 minutes is normal, not stuck.

## Step by step

1. Install **Android Studio** and open it.
2. **File → Open** → select the `Bloomodoro/Bloomodoro` folder (the one containing
   `settings.gradle.kts`).
3. Let Gradle sync finish (progress bar at the bottom). If it flags an out-of-date
   dependency, click the offered "Update" quick-fix and re-sync.
4. On your phone: **Settings → About phone** → tap **Build number** 7 times to unlock
   Developer Options → back out to **Settings → Developer options** → enable
   **USB debugging**.
5. Plug your phone into the computer with USB. A prompt appears on the phone asking to
   trust the computer — accept it.
6. In Android Studio's toolbar, the device dropdown (top, next to the Run ▶ button) should
   now show your phone's name. Select it.
7. Make sure the run configuration dropdown next to it says **androidApp**.
8. Click **Run ▶**. Android Studio builds a debug APK and installs it straight onto your
   phone — you'll see Bloomodoro open automatically.
9. On first launch it'll ask for the notification permission (needed for session/stretch/
   deadline alarms) — allow it. On some phones you'll also want to check
   **Settings → Apps → Bloomodoro → Alarms & reminders** is allowed, so exact-time alarms
   (stretch reminders, class/deadline alarms) fire precisely.

That's it — every rebuild after this is just Run ▶ again while the phone is plugged in.

### Building a standalone APK (no cable needed afterward)

If you'd rather install it once and then disconnect: **Build → Build App Bundle(s) / APK(s)
→ Build APK(s)**. Android Studio drops the file in
`androidApp/build/outputs/apk/debug/androidApp-debug.apk` — copy that to your phone (e.g. via
a cloud drive) and tap it there to install (you'll need to allow "install unknown apps" for
whichever app you used to transfer it, one time).

---

## Desktop (fully offline, easiest to try first)

```bash
cd Bloomodoro
./gradlew :desktopApp:run
```

## iOS (needs a Mac + Xcode)

Open `iosApp/` in Xcode, pick your iPhone as the run target, hit Run. If Xcode still lists a
`GoogleSignIn` Swift Package dependency from an earlier version of this project, remove it
(File → Add Packages… → select it → Remove) since it's no longer used.

## Turning on email backup (now wired in — just needs your own Firebase project)

Backup & cross-device sync is fully implemented (email sign-up/sign-in, "بک‌آپ بگیر الان" /
"بازیابی از بک‌آپ" buttons under Settings → بک‌آپ و همگام‌سازی) and builds out of the box using
a **placeholder** `androidApp/google-services.json` — so the app compiles and runs with zero
setup, but sign-up/sign-in will show a clear error until you swap in your own project:

1. Go to the [Firebase console](https://console.firebase.google.com/) → **Add project**.
2. Inside it, **Build → Authentication → Get started → Email/Password → Enable**.
3. **Build → Firestore Database → Create database** (any region; start in production mode —
   `backend/firestore.rules` already locks each backup to its own signed-in user).
4. **Project settings → Your apps → Add app → Android**, package name `com.bloomodoro.android`,
   download the real `google-services.json`, and replace
   `androidApp/google-services.json` with it.
5. For iOS: **Add app → iOS**, bundle id whatever you set up in Xcode, download
   `GoogleService-Info.plist`, drop it into `iosApp/iosApp/`, and add one line to
   `AppDelegate.swift`'s `didFinishLaunchingWithOptions`: `FirebaseApp.configure()` (needs
   `import FirebaseCore` and the `FirebaseCore`/`FirebaseAuth`/`FirebaseFirestore` Swift
   Packages added in Xcode — Android is the fully-tested path today, iOS needs this one extra
   step since Firebase's iOS SDK initializes itself from Swift, not Kotlin).
6. Rebuild — sign-up/sign-in/backup/restore now talk to your real project.

Nothing else in the app depends on this — every other feature works fully offline whether or
not backup is ever turned on.
