package com.odak.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object ImageStorage {

    private fun photosDir(context: Context): File =
        File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }

    /** Creates an empty file inside the app's photos dir and returns it with a shareable Uri. */
    fun newCameraTarget(context: Context): Pair<File, Uri> {
        val file = File(photosDir(context), "IMG_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    /** Copies an external (gallery) image into app storage and returns the absolute path. */
    fun importFromUri(context: Context, uri: Uri): String? {
        return try {
            val file = File(photosDir(context), "IMG_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deleteQuietly(path: String?) {
        if (path == null) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
