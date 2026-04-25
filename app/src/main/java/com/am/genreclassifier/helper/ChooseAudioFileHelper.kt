package com.am.genreclassifier.helper

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class ChooseAudioFileHelper(private val activity: ComponentActivity) {

    fun chooseFromStorage(onFileChosen: (waveFile: File, fileName: String) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/x-wav"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        onChosen = onFileChosen
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                val fileName = queryFileName(uri) ?: "selected_audio.wav"
                val targetFile = File(activity.cacheDir, fileName)

                activity.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                targetFile.setReadable(true, false)
                onChosen(targetFile, fileName)
            }
        }

    private fun queryFileName(uri: android.net.Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    companion object {
        private lateinit var onChosen: (File, String) -> Unit
    }
}