package com.bloomodoro.platform

import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings

actual fun createSettings(context: PlatformContext): Settings {
    val prefs = context.context.getSharedPreferences("bloomodoro_prefs", android.content.Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
