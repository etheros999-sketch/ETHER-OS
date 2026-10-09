package com.darkempire.ether

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class FinanceEntry(
    val id: Long,
    val description: String,
    val amount: Double,
    val currency: String,
    val source: String,
    val payoutMethod: String,
    val received: Boolean = false
)

internal object FinanceStore {
    private const val PREFS = "ether_finance"
    private const val KEY_ENTRIES = "entries"

    fun load(context: Context): List<FinanceEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    val description = item.optString("description").trim()
                    val amount = item.optDouble("amount", Double.NaN)
                    val currency = item.optString("currency", "GHS")
                    if (description.isNotEmpty() && amount.isFinite() && amount > 0 && currency in setOf("GHS", "USD")) {
                        add(
                            FinanceEntry(
                                id = item.optLong("id", i.toLong()),
                                description = description,
                                amount = amount,
                                currency = currency,
                                source = item.optString("source"),
                                payoutMethod = item.optString("payoutMethod"),
                                received = item.optBoolean("received", false)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, entries: List<FinanceEntry>) {
        val json = JSONArray()
        entries.forEach { entry ->
            json.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("description", entry.description)
                    .put("amount", entry.amount)
                    .put("currency", entry.currency)
                    .put("source", entry.source)
                    .put("payoutMethod", entry.payoutMethod)
                    .put("received", entry.received)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENTRIES, json.toString())
            .apply()
    }
}
