package com.questua.app.core.common

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.util.Patterns

/**
 * Extension function para converter uma URI (da galeria) em um File (temporário)
 * para ser enviado via Multipart no Retrofit.
 */
fun Context.uriToFile(uri: Uri): File? {
    return try {
        val contentResolver = this.contentResolver
        val myFile = File(this.cacheDir, getFileName(uri, contentResolver))

        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(myFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        myFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(uri: Uri, contentResolver: android.content.ContentResolver): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Tenta pegar o nome real do arquivo
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "temp_file_${System.currentTimeMillis()}.jpg"
}

fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null

    if (this.startsWith("http")) return this


    val serverUrl = Constants.BASE_URL.replace("/api/", "")

    val cleanPath = if (this.startsWith("/")) this.substring(1) else this

    return "$serverUrl/$cleanPath"
}

fun String.isValidEmail(): Boolean {

    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

}


// Formatação ou Capitalização

fun String.capitalizeFirstLetter(): String {

    return this.lowercase().replaceFirstChar { it.uppercase() }

}


// Extensão para tratar erros de API genéricos se necessário

fun Throwable.userFriendlyMessage(): String {

    return this.localizedMessage ?: "Ocorreu um erro inesperado"
}