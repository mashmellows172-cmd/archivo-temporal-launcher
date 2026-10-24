package com.archivotemporal.launcher.character

import kotlin.math.roundToInt

internal object MissMinutesMotion {
    const val POINTING_MILLIS = 190
    const val CONFIRMING_MILLIS = 900L
    const val NOTICE_MILLIS = 2_500L
    const val SLEEP_AFTER_MILLIS = 30_000L

    fun movementMillis(distance: Float): Int =
        (220 + distance.coerceIn(0f, 1f) * 140).roundToInt().coerceIn(220, 360)
}
