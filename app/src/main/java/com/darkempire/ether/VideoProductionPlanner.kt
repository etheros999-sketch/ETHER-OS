package com.darkempire.ether

/**
 * Builds the explicit production brief sent to the AI planner.
 * This creates a plan only; it never claims that media was rendered or published.
 */
internal object VideoProductionPlanner {
    fun buildPrompt(niche: String, format: String, targets: String): String {
        require(niche.isNotBlank()) { "Enter a channel niche first." }
        val safeNiche = niche.trim()
        val safeFormat = format.trim().ifBlank { "Both long + short" }
        val safeTargets = targets.trim().ifBlank { "YouTube first" }

        return "Act as ETHER's faceless video-business production planner. " +
            "Create a practical, original, evidence-aware production package for this niche: $safeNiche. " +
            "Required output format: $safeFormat. Publishing targets: $safeTargets. " +
            "If both long and short are selected, provide a complete 5-8 minute YouTube video package " +
            "and a separate 30-60 second vertical Short/TikTok cutdown based on the same idea. " +
            "Include: 3 topic ideas ranked by audience value, title options, opening hook, complete narration script, " +
            "scene-by-scene visual and B-roll directions, on-screen text, narration tone, caption/subtitle guidance, " +
            "thumbnail concept and image-generation prompt, description, hashtags, call to action, factual checks, " +
            "copyright/licensing checks, and a final quality-control checklist. " +
            "Do not claim that a video file was rendered, exported, uploaded, scheduled, or published. " +
            "Clearly label this as a production blueprint. Avoid invented facts and suggest checking claims against reliable sources."
    }
}
