package com.bloomodoro.platform

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(context: PlatformContext): Settings =
    PreferencesSettings(Preferences.userRoot().node("bloomodoro"))
