# 🌸 Bloomodoro

A personal, fully customizable study/work timer built with **Kotlin Multiplatform** and
**Compose Multiplatform** — one shared codebase running natively on **Android**, **iOS**,
and **Desktop** (macOS/Windows/Linux).

Persian-first UI · Bilingual (فارسی / English) · Jalali & Gregorian calendars.

<p>
  <img src="https://img.shields.io/badge/Kotlin_Multiplatform-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Compose_Multiplatform-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/iOS-000000?style=flat-square&logo=apple&logoColor=white" />
  <img src="https://img.shields.io/badge/Desktop-0078D4?style=flat-square&logo=windows&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" />
</p>

---

## ✨ Features

- **Two fully independent modes — Study & Work.** Each has its own focus/break durations,
  notification rules, and stretch-reminder interval. Switching modes never mixes their settings
  or their history.
- **Three-moment notifications** — start / midpoint / end of every session, scheduled at the
  OS level so they arrive even if the app is killed.
- **Live focus display** — while a session runs, an ongoing notification (visible on the lock
  screen on Android) shows what's running and counts down to the end.
- **Session-end summary** — when a work session finishes (even if you'd closed the app), you
  get a notification with your total focused time for the day.
- **Stand up & stretch reminders** — a repeating reminder every N minutes *during* a work
  session, so you actually get up and move your neck/back. Fully configurable per mode.
- **The garden** — every day you focus, a little flower grows for that day; the more minutes,
  the bigger the plant (🌱 → 🌿 → 🌷 → 🌸 → 🌳). Each one pops in with a little grow-in animation
  and sways gently forever after — every plant's timing is offset slightly so the whole garden
  reads as a living breeze, not a synced loop. Over time it's a small forest of your own effort.
- **Bilingual — فارسی + English**, switchable anytime in Settings. Persian renders fully
  right-to-left, English left-to-right — the whole app's layout direction flips, not just the
  text alignment.
- **Jalali (Shamsi) + Gregorian calendars**, also switchable in Settings. History and deadline
  dates render in whichever system you pick, with the Jalali week correctly starting on
  Saturday (its own conversion routine, no external dependency).
- **Goal Journey** — a second, freer way to plan besides the Year→Week pyramid: name a
  destination ("Apply abroad"), then lay out the concrete steps to get there (language exam,
  articles, recommendation letters...) as an animated path. Steps pop, the current one pulses
  as "you are here", the trail fills in as you go, and finishing the last step triggers a small
  celebration animation.
- **Full history** — every session ever logged, filterable by mode, with daily/weekly/total
  minutes.
- **Manual daily target** — tell the app "I want to focus 3 hours today" and watch a live
  progress bar toward it.
- **Planning suite**
  - **To-do list** per mode.
  - **Weekly/daily schedule** — recurring blocks (e.g. "Mondays 9–11: Math").
  - **Classes & deadlines** — each one rings its own alarm on the day it's due.
  - **Goal pyramid** — Year → Season → Month → Week. Set a yearly goal ("read 12 books"),
    then add the season/month/week steps underneath it and track progress on each level —
    a literal pyramid visualization of what you need to hit this year's target.
- **Deep theming** — curated color palettes or fully custom colors; light/dark; a little
  companion (🌸 bloom / 🐱 kitten / 🟢 blob) that grows with completed sessions.
- **Calendar integration** — add the running session to your device calendar with one tap.
- **Offline-first** — everything above works with zero internet connection.
- **Cloud backup & sync** — sign up/sign in with email (Settings → بک‌آپ و همگام‌سازی), back up
  your garden/history/todos/schedule/goals/settings to the cloud, and restore them on a new
  device. Built on Firebase Auth + Firestore. `google-services.json` is not committed — add your own; see
  [`docs/SETUP.md`](docs/SETUP.md) for the ~5-minute steps to point it at your own free
  Firebase project.
- **A proper lock-screen live timer** — while a session runs, a real Android foreground
  service keeps a chronometer-style countdown on the lock screen, durable even if the app is
  swiped away.
- **A custom app icon** — the rose/clock badge is wired in as the Android adaptive icon and
  the desktop app icon (Windows `.ico`, macOS `.icns`, Linux `.png`), replacing the generic
  default icon everywhere.

## 🏗️ Architecture

```
shared/         Kotlin Multiplatform core — domain logic + Compose Multiplatform UI
  commonMain/   Timer engine, planning manager, models, theming, i18n, all screens
  androidMain/  Android actuals: AlarmManager notifications, ongoing lock-screen notification
  iosMain/      iOS actuals: UNUserNotificationCenter
  desktopMain/  Desktop actuals: in-process scheduling, local-only persistence

androidApp/     Thin Android application module (MainActivity, manifest)
desktopApp/     Thin Compose Desktop application module
iosApp/         Swift files for the iOS app shell (Xcode project not included — see docs/SETUP.md)
backend/        Firebase: Firestore rules for the phase-2 backup feature only
docs/           Full setup walkthrough and project notes
```

**Key design decision:** the timer never relies on a long-running background process. Every
phase transition is computed from a persisted `(mode, phase, startTimestamp, duration)` tuple,
so the UI re-derives the correct remaining time even after the app was fully killed and
reopened. Notification *delivery* is scheduled at the OS level (`AlarmManager` on Android,
`UNUserNotificationCenter` on iOS) independently of whether the app process is alive.

### What works on each platform today

| | Android | iOS | Desktop |
|---|---|---|---|
| Timer, garden, planning, history, themes | ✅ | ✅ | ✅ (fully offline) |
| OS-scheduled notifications + stretch reminders | ✅ | ✅ | ⚠️ in-process only |
| Live lock-screen focus display | ✅ (real foreground service + chronometer) | ⚠️ end-of-phase only* | N/A (shown in-window instead) |
| Email backup & sync | ✅ (needs your own Firebase project — 5 min setup) | ✅ (needs one extra `FirebaseApp.configure()` line, see SETUP.md) | not available |

\* iOS has no public API for a second-by-second ongoing notification the way Android's
`setOngoing` + chronometer does; a true live countdown there needs a small Live Activity /
Dynamic Island widget extension (a few dozen lines of Swift+ActivityKit) — noted as a
follow-up in `docs/SETUP.md`.

## 🚀 Quickstart

```bash
git clone https://github.com/negarsh76/Bloomodoro.git
cd Bloomodoro
./gradlew :desktopApp:run        # runs immediately, fully offline, no setup needed
```

For **Android**: open the project root in Android Studio, let Gradle sync (already pinned to
Gradle 8.9 in `gradle/wrapper/gradle-wrapper.properties`), plug in your phone with USB
debugging on, and hit Run ▶ on the `androidApp` configuration. Full walkthrough, including
enabling Developer Options/USB debugging on your phone, is in
**[`docs/SETUP.md`](docs/SETUP.md)**.

For **iOS**: open `iosApp/` in Xcode (needs a Mac). Note the Google Sign-In package reference
was removed from the Kotlin side; if the Xcode project still lists `GoogleSignIn` as a Swift
Package dependency, remove it there too (File → Add Packages... → select it → Remove).

## 🎨 Customizing

| I want to change… | Where |
|---|---|
| Default color palettes | `shared/.../theme/Palettes.kt` |
| Default phrases | `shared/.../domain/PhraseProvider.kt` |
| Default timer durations (per mode) | `shared/.../model/Models.kt` → `TimerConfig` |
| Companion emoji stages | `shared/.../ui/TimerScreen.kt` → `BloomCompanion` |
| Garden flower thresholds | `shared/.../ui/GardenScreen.kt` → `FlowerTile` |
| UI strings | `shared/.../i18n/Strings.kt` |
| Corner radius / typography | `shared/.../ui/AppTheme.kt` |

## 🗺️ Roadmap

- [x] Turn on email backup/restore end-to-end (UI screen + wiring `CloudSync`) — done, needs your own Firebase project
- [ ] iOS Live Activity for a true second-by-second lock-screen countdown
- [ ] Android home-screen widget showing today's garden
- [ ] Weekly calendar-style view for the schedule (currently a flat list)

## 🤝 Contributing

This started as a personal project, but PRs are welcome — see [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 📄 License

[MIT](LICENSE)

---

<p align="center">Made with 🌸 by <a href="https://github.com/negarsh76">Negar Shojaei</a></p>
