package com.bloomodoro.platform

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun createSettings(context: PlatformContext): Settings =
    NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
