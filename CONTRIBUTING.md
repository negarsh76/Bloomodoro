# Contributing to Bloomodoro

Thanks for taking a look! This started as a personal project, so the bar for contributions
is kept intentionally light.

## Before you open a PR

- Run a Gradle sync locally and make sure `:androidApp` and `:desktopApp` build.
- Keep new user-facing strings in `shared/src/commonMain/kotlin/com/bloomodoro/i18n/Strings.kt`
  — add both a Persian and an English value so the app stays bilingual.
- If you touch the timer engine (`domain/PomodoroEngine.kt`), please explain the change in the
  PR description — its correctness depends on timestamp math, not on a running loop, so it's
  easy to introduce a subtle drift bug.
- Match the existing code style (no linter is enforced yet — just follow what's around your change).

## Reporting a bug

Open an issue with:
- Platform (Android / iOS / Desktop) and OS version
- What you expected vs. what happened
- The full error/stack trace if there is one

## Ideas / feature requests

Open an issue describing the use case before sending a large PR — happy to discuss direction
first so we don't duplicate effort.
