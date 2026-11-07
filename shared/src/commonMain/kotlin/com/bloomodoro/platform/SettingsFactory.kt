package com.bloomodoro.platform

import com.russhwolf.settings.Settings

/** Returns a real, persistent Settings instance for this platform (SharedPreferences / NSUserDefaults / Java Preferences). */
expect fun createSettings(context: PlatformContext): Settings
