package com.odak.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    /** Latest short (7-char) commit SHA on the repo's main branch, or null on failure. */
    fun fetchLatestSha(repo: String): String? = try {
        val conn = (URL("https://api.github.com/repos/$repo/commits/main")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Odak-App")
            connectTimeout = 15000
            readTimeout = 15000
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        JSONObject(body).getString("sha").take(7)
    } catch (e: Exception) {
        null
    }

    /** Downloads the latest APK into internal storage and returns the file. */
    fun downloadApk(context: Context, apkUrl: String): File {
        val dir = File(context.filesDir, "updates").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Odak-update.apk")
        val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 20000
            readTimeout = 60000
        }
        conn.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
        return file
    }

    /** Launches the system installer for the downloaded APK. */
    fun installApk(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
