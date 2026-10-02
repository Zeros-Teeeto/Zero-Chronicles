package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class StorageManager(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val savesDir: File
        get() = File(context.filesDir, "saves").apply { if (!exists()) mkdirs() }

    private val templatesDir: File
        get() = File(context.filesDir, "world_templates").apply { if (!exists()) mkdirs() }

    private val configFile: File
        get() = File(context.filesDir, "config_keys.json")

    // --- App Config / API Keys ---
    fun loadConfig(): AppConfig {
        return try {
            val cfg = if (configFile.exists()) {
                val json = configFile.readText()
                val adapter = moshi.adapter(AppConfig::class.java)
                adapter.fromJson(json) ?: AppConfig()
            } else {
                AppConfig()
            }
            val cleanKeys = cfg.apiKeys.filter { it.isNotBlank() }
            val updated = cfg.copy(apiKeys = cleanKeys)
            if (updated != cfg) {
                saveConfig(updated)
            }
            updated
        } catch (e: Exception) {
            AppConfig()
        }
    }

    fun saveConfig(config: AppConfig) {
        try {
            val adapter = moshi.adapter(AppConfig::class.java)
            val json = adapter.toJson(config)
            configFile.writeText(json)
        } catch (_: Exception) {}
    }

    // --- Game Sessions (Saves) ---
    fun saveSession(session: GameSession) {
        try {
            val file = File(savesDir, "save_${session.id}.json")
            val adapter = moshi.adapter(GameSession::class.java)
            val json = adapter.toJson(session)
            file.writeText(json)
        } catch (_: Exception) {}
    }

    fun loadAllSessions(): List<GameSession> {
        val list = mutableListOf<GameSession>()
        val files = savesDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        val adapter = moshi.adapter(GameSession::class.java)
        for (file in files.sortedByDescending { it.lastModified() }) {
            try {
                val session = adapter.fromJson(file.readText())
                if (session != null) {
                    list.add(session)
                }
            } catch (_: Exception) {}
        }
        return list
    }

    fun loadSession(id: String): GameSession? {
        val file = File(savesDir, "save_$id.json")
        if (!file.exists()) return null
        return try {
            val adapter = moshi.adapter(GameSession::class.java)
            adapter.fromJson(file.readText())
        } catch (e: Exception) {
            null
        }
    }

    fun deleteSession(id: String): Boolean {
        val file = File(savesDir, "save_$id.json")
        return if (file.exists()) file.delete() else false
    }

    // --- World Templates ---
    fun saveTemplate(template: WorldTemplate) {
        try {
            val file = File(templatesDir, "tpl_${template.id}.json")
            val adapter = moshi.adapter(WorldTemplate::class.java)
            val json = adapter.toJson(template)
            file.writeText(json)
        } catch (_: Exception) {}
    }

    fun loadAllTemplates(): List<WorldTemplate> {
        val list = mutableListOf<WorldTemplate>()
        val files = templatesDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        val adapter = moshi.adapter(WorldTemplate::class.java)
        for (file in files.sortedByDescending { it.lastModified() }) {
            try {
                val tpl = adapter.fromJson(file.readText())
                if (tpl != null) list.add(tpl)
            } catch (_: Exception) {}
        }
        return list
    }

    fun deleteTemplate(id: String): Boolean {
        val file = File(templatesDir, "tpl_$id.json")
        return if (file.exists()) file.delete() else false
    }
}
