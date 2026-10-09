package com.darkempire.ether

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoProductionPlannerTest {
    @Test
    fun `prompt includes niche and both output types`() {
        val prompt = VideoProductionPlanner.buildPrompt(
            niche = "beginner technology",
            format = "Both long + short",
            targets = "YouTube + TikTok"
        )

        assertTrue(prompt.contains("beginner technology"))
        assertTrue(prompt.contains("Both long + short"))
        assertTrue(prompt.contains("YouTube + TikTok"))
        assertTrue(prompt.contains("5-8 minute YouTube video"))
        assertTrue(prompt.contains("30-60 second vertical Short/TikTok cutdown"))
    }

    @Test
    fun `prompt makes no false claim of publication`() {
        val prompt = VideoProductionPlanner.buildPrompt("history", "Long videos", "YouTube first")

        assertTrue(prompt.contains("Do not claim that a video file was rendered"))
        assertTrue(prompt.contains("production blueprint"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank niche is rejected`() {
        VideoProductionPlanner.buildPrompt("  ", "Both long + short", "YouTube + TikTok")
    }

    @Test
    fun `blank options use safe defaults`() {
        val prompt = VideoProductionPlanner.buildPrompt("science", "", "")

        assertTrue(prompt.contains("Required output format: Both long + short"))
        assertTrue(prompt.contains("Publishing targets: YouTube first"))
    }
}
