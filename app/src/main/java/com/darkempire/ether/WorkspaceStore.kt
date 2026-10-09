package com.darkempire.ether

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class WorkspaceTask(
    val id: Long,
    val title: String,
    val done: Boolean = false
)

internal object WorkspaceStore {
    private const val PREFS = "ether_workspace"
    private const val KEY_TASKS = "tasks"

    fun load(context: Context): List<WorkspaceTask> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    val title = item.optString("title").trim()
                    if (title.isNotEmpty()) {
                        add(
                            WorkspaceTask(
                                id = item.optLong("id", i.toLong()),
                                title = title,
                                done = item.optBoolean("done", false)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, tasks: List<WorkspaceTask>) {
        val json = JSONArray()
        tasks.forEach { task ->
            json.put(
                JSONObject()
                    .put("id", task.id)
                    .put("title", task.title)
                    .put("done", task.done)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TASKS, json.toString())
            .apply()
    }
}
