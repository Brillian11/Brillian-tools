package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object NoteAttachmentHelper {

    private fun getAttachmentDir(context: Context): File {
        val dir = File(context.filesDir, "note_attachments")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveUriToInternalStorage(context: Context, uri: Uri, prefix: String = "att"): File? {
        return try {
            val resolver = context.contentResolver
            val extension = when (resolver.getType(uri)) {
                "image/png" -> "png"
                "image/jpeg", "image/jpg" -> "jpg"
                "image/webp" -> "webp"
                "application/pdf" -> "pdf"
                else -> {
                    val name = uri.lastPathSegment ?: ""
                    if (name.contains(".")) name.substringAfterLast(".") else "bin"
                }
            }
            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"
            val targetFile = File(getAttachmentDir(context), fileName)

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openPdfFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "PDF file not found on device", Toast.LENGTH_SHORT).show()
                return
            }
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No app available to open PDF view", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFileName(filePath: String): String {
        return File(filePath).name
    }

    fun getFileSizeFormatted(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) return "0 KB"
        val bytes = file.length()
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val df = DecimalFormat("#.##")
        return if (mb >= 1.0) {
            "${df.format(mb)} MB"
        } else {
            "${df.format(kb)} KB"
        }
    }

    fun parsePaths(pathsString: String): List<String> {
        if (pathsString.isBlank()) return emptyList()
        return pathsString.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun joinPaths(paths: List<String>): String {
        return paths.filter { it.isNotBlank() }.joinToString(",")
    }
}
