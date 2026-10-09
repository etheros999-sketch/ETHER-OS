package com.darkempire.ether

/**
 * Returns a greeting for the device's local 24-hour clock.
 * Kept independent of UI code so the time boundaries can be unit-tested.
 */
internal fun greetingForHour(hour: Int): String {
    require(hour in 0..23) { "Hour must be between 0 and 23." }
    return when (hour) {
        in 5..11 -> "Good morning, Dark Emperor."
        in 12..16 -> "Good afternoon, Dark Emperor."
        in 17..21 -> "Good evening, Dark Emperor."
        else -> "Still here, Dark Emperor?"
    }
}
