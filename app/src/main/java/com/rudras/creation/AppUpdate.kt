package com.rudras.creation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdate(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String
)

object UpdateChecker {
    suspend fun check(currentVersionCode: Int): AppUpdate? = withContext(Dispatchers.IO) {
        val manifestUrl = BuildConfig.UPDATE_MANIFEST_URL.trim()
        if (manifestUrl.isBlank() || !manifestUrl.startsWith("https://")) return@withContext null

        runCatching<AppUpdate?> {
            val connection = URL(manifestUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.requestMethod = "GET"
            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val versionCode = json.optInt("versionCode", 0)
                val apkUrl = json.optString("apkUrl").trim()
                if (versionCode <= currentVersionCode || !apkUrl.startsWith("https://")) null else AppUpdate(
                    versionCode = versionCode,
                    versionName = json.optString("versionName", ""),
                    apkUrl = apkUrl,
                    notes = json.optString("notes", "")
                )
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
}

