# Why isn't there an .xcodeproj here?

Xcode's project file format is a fragile binary/plist structure that's a bad fit for hand-
authoring — the standard, reliable approach is to generate it yourself with Xcode (about
10 minutes) and drop the Swift files from this folder into it. Full steps are in
`docs/SETUP.md` → "Part 7 — Set up iOS on a Mac". Short version:

1. In Xcode: File → New → Project → iOS → App (name it `iosApp`, Interface = SwiftUI,
   Language = Swift), and save it directly inside this repo's `iosApp/` folder (replacing
   this stub folder's contents).
2. Replace the default `iOSApp.swift` / `ContentView.swift` with the versions in this folder,
   and add `AppDelegate.swift` + `GoogleSignInOnboardingView.swift`.
3. Add a "Run Script" Build Phase before "Compile Sources" that invokes Gradle to build and
   embed the Kotlin framework (`Shared.framework`) — exact script in `docs/SETUP.md`.
4. Add these Swift packages via SPM: `firebase-ios-sdk` (FirebaseCore, FirebaseMessaging) and
   `GoogleSignIn-iOS`.
5. Add the `GoogleService-Info.plist` you downloaded from the Firebase Console.
6. Merge this folder's `Info.plist` contents (especially `CFBundleURLTypes`) into the one
   Xcode generated.
7. Don't forget to add the **Push Notifications** capability under Signing & Capabilities —
   it's easy to miss and partner pings won't arrive without it.

Then hit Run — you'll see the same shared Compose UI that runs on Android and Desktop.
