package com.dmitrysukhov.lifetracker.utils

import android.content.Context
import android.net.Uri
import java.io.File

object ImageUtils {
    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "project_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return fileName
    }

    fun deleteImageFromInternalStorage(context: Context, fileName: String) {
        File(context.filesDir, fileName).delete()
    }
}