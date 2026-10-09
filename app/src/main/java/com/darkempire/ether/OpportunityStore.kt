package com.darkempire.ether

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class Opportunity(
    val id: Long,
    val title: String,
    val platform: String,
    val budget: String,
    val url: String,
    val details: String,
    val status: String = "New",
    val proposal: String = ""
)

internal object OpportunityStore {
    private const val PREFS = "ether_opportunities"
    private const val KEY_ITEMS = "items"

    fun load(context: Context): List<Opportunity> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    val title = item.optString("title").trim()
                    if (title.isNotEmpty()) {
                        add(
                            Opportunity(
                                id = item.optLong("id", i.toLong()),
                                title = title,
                                platform = item.optString("platform", "Other"),
                                budget = item.optString("budget"),
                                url = item.optString("url"),
                                details = item.optString("details"),
                                status = item.optString("status", "New"),
                                proposal = item.optString("proposal")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, opportunities: List<Opportunity>) {
        val json = JSONArray()
        opportunities.forEach { item ->
            json.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("platform", item.platform)
                    .put("budget", item.budget)
                    .put("url", item.url)
                    .put("details", item.details)
                    .put("status", item.status)
                    .put("proposal", item.proposal)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, json.toString())
            .apply()
    }
}
