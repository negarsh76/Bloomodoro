package com.bloomodoro.theme

import com.bloomodoro.model.ColorTheme

/**
 * Every value here is just a *starting point* — the Theme screen lets the user override
 * any single colour and save it as their own custom palette. These five presets exist so
 * the app feels finished on first launch, each with its own clear personality.
 */
object Palettes {

    val peachCream = ColorTheme(
        id = "peach_cream",
        name = "Peach Cream",
        background = 0xFFFBF3EC,
        surface = 0xFFFFFFFF,
        primary = 0xFFE2725B,   // terracotta rose
        secondary = 0xFF8FA998, // sage
        accentBloom = 0xFFF4A6B7,
        isDark = false,
    )

    val lavenderMist = ColorTheme(
        id = "lavender_mist",
        name = "Lavender Mist",
        background = 0xFFF3EEFB,
        surface = 0xFFFFFFFF,
        primary = 0xFF8E7CC3,
        secondary = 0xFFC9B6E4,
        accentBloom = 0xFFF7A8C4,
        isDark = false,
    )

    val matchaSoda = ColorTheme(
        id = "matcha_soda",
        name = "Matcha Soda",
        background = 0xFFF1F7EC,
        surface = 0xFFFFFFFF,
        primary = 0xFF5C8A55,
        secondary = 0xFFB7D6A8,
        accentBloom = 0xFFFFD37A,
        isDark = false,
    )

    val midnightBerry = ColorTheme(
        id = "midnight_berry",
        name = "Midnight Berry",
        background = 0xFF1C1622,
        surface = 0xFF2A2230,
        primary = 0xFFE38AAE,
        secondary = 0xFF8B7BB8,
        accentBloom = 0xFFFFB6C9,
        isDark = true,
    )

    val honeyAmber = ColorTheme(
        id = "honey_amber",
        name = "Honey Amber",
        background = 0xFFFFF6E5,
        surface = 0xFFFFFFFF,
        primary = 0xFFD98E3D,
        secondary = 0xFFE7B65B,
        accentBloom = 0xFFE2725B,
        isDark = false,
    )

    val all = listOf(peachCream, lavenderMist, matchaSoda, midnightBerry, honeyAmber)

    fun byId(id: String): ColorTheme = all.firstOrNull { it.id == id } ?: peachCream

    /**
     * Applies the user's manual dark/light preference on top of whichever palette they picked.
     * Every preset (and any fully-custom theme) can be viewed in either mode: light palettes get
     * a proper dark background/surface while keeping their accent hues recognisable, and dark
     * palettes can be viewed in light mode the same way — nothing is hard-locked to one mode.
     */
    fun ColorTheme.withDarkPreference(wantDark: Boolean): ColorTheme {
        if (isDark == wantDark) return this
        return if (wantDark) {
            copy(background = 0xFF17131A, surface = 0xFF241E29, isDark = true)
        } else {
            copy(background = 0xFFFBF3EC, surface = 0xFFFFFFFF, isDark = false)
        }
    }
}
