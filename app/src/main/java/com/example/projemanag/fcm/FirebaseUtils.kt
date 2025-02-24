package com.example.projemanag.fcm

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FirebaseUtils {
    // ✅ Copy service_account.json from assets to internal storage
    fun copyServiceAccountJsonToFiles(context: Context) {
        val fileName = "service-account.json"
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {  // Copy only if not already copied
            try {
                val assetManager = context.assets
                val inputStream: InputStream = assetManager.open(fileName)
                val outputStream: OutputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                inputStream.close()
                outputStream.close()
                Log.d("FirebaseUtils", "✅ service_account.json copied to: ${file.absolutePath}")

            } catch (e: IOException) {
                Log.e("FirebaseUtils", "❌ Error copying service_account.json: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.d("FirebaseUtils", "⚠️ service_account.json already exists in files directory.")
        }
    }
}