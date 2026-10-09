package com.darkempire.ether

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun morningWindowUsesMorningGreeting() {
        assertEquals("Good morning, Dark Emperor.", greetingForHour(5))
        assertEquals("Good morning, Dark Emperor.", greetingForHour(11))
    }

    @Test
    fun afternoonWindowUsesAfternoonGreeting() {
        assertEquals("Good afternoon, Dark Emperor.", greetingForHour(12))
        assertEquals("Good afternoon, Dark Emperor.", greetingForHour(16))
    }

    @Test
    fun eveningWindowUsesEveningGreeting() {
        assertEquals("Good evening, Dark Emperor.", greetingForHour(17))
        assertEquals("Good evening, Dark Emperor.", greetingForHour(21))
    }

    @Test
    fun overnightWindowUsesNeutralGreeting() {
        assertEquals("Still here, Dark Emperor?", greetingForHour(22))
        assertEquals("Still here, Dark Emperor?", greetingForHour(4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidHour() {
        greetingForHour(24)
    }
}
