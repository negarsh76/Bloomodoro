package com.bloomodoro.platform

/**
 * A thin final wrapper around the real android.content.Context, mirroring the shape of the
 * iOS/Desktop actuals (both plain, instantiable classes). We deliberately do NOT use a
 * typealias here: android.content.Context is abstract, and an expect class's actual must
 * match its modality exactly — a typealias to an abstract class conflicts with the (implicitly
 * final) common `expect class PlatformContext`. Wrapping keeps all three platforms consistent
 * and instantiable the same way (`PlatformContext(...)` / `PlatformContext()`).
 */
actual class PlatformContext(val context: android.content.Context)
