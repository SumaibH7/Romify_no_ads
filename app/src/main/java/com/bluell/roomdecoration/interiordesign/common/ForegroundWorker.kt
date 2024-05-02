package com.bluell.roomdecoration.interiordesign.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bluell.roomdecoration.interiordesign.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class ForegroundWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    var file: File? = null
    private var isException = false
    private var contentLengthTotal: Long = 0
    private var localizedMessage = "unexpected error"
    private var totalFiles: Int = 0
    private val notificationManager =
        appContext.getSystemService(NotificationManager::class.java)

    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Downloading...!Please wait")

    private lateinit var notification: Notification

    override suspend fun doWork(): Result {
        Log.d(TAG, "Started job")
        try {
            createNotificationChannel()
            contentLengthTotal = inputData.getLong("TotalSizeOfFiles", 0)
            val type = object : TypeToken<MutableList<String>>() {}.type
            val list = Gson().fromJson<MutableList<String>>(inputData.getString("url"), type)

            totalFiles = list.size
            var result = Result.failure()
            for (url in list) {
                result = downloadFileFromUri(url)
            }

            return result
        } catch (e: Exception) {
            return Result.failure(workDataOf("error" to e.localizedMessage))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = notificationManager?.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel == null) {
                notificationManager?.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    private suspend fun downloadFileFromUri(
        urlImage: String
    ): Result {
        notification = notificationBuilder
            .build()
        val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification)

        setForeground(foregroundInfo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val url = URL(urlImage)
                val urlConnection: URLConnection =
                    withContext(Dispatchers.IO) {
                        url.openConnection()
                    }
                withContext(Dispatchers.IO) {
                    urlConnection.connect()
                }

                val inputStream: InputStream = BufferedInputStream(withContext(Dispatchers.IO) {
                    urlConnection.getInputStream()
                })

                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        System.currentTimeMillis().toString() + ".png"
                    )
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Room Design"
                    )
                }

                val resolver = appContext.contentResolver
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { outputStream ->
                        val buffer = ByteArray(54 * 1024 * 1024)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    inputStream.close()
                }

                return Result.success()
            } catch (e: IOException) {
                isException = true
                localizedMessage = "File in URL not found"
                return Result.failure(workDataOf("error" to localizedMessage))
            }

        } else {
            try {
                val url = URL(urlImage)
                val urlConnection: URLConnection = withContext(Dispatchers.IO) {
                    url.openConnection()
                }
                withContext(Dispatchers.IO) {
                    urlConnection.connect()
                }

                val inputStream: InputStream = BufferedInputStream(withContext(Dispatchers.IO) {
                    urlConnection.getInputStream()
                })

                val appContext = applicationContext

                val picturesDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Room Design")

                if (picturesDirectory != null && !picturesDirectory.exists()) {
                    picturesDirectory.mkdirs()
                }

                val fileName = System.currentTimeMillis().toString() + ".png"

                val destinationFile = File(picturesDirectory, fileName)

                val outputStream = withContext(Dispatchers.IO) {
                    FileOutputStream(destinationFile)
                }

                val buffer = ByteArray(16 * 1024)
                var bytesRead: Int
                while (withContext(Dispatchers.IO) {
                        inputStream.read(buffer)
                    }.also { bytesRead = it } != -1) {
                    withContext(Dispatchers.IO) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }

                withContext(Dispatchers.IO) {
                    inputStream.close()
                    outputStream.close()
                }

                MediaScannerConnection.scanFile(
                    appContext,
                    arrayOf(destinationFile.absolutePath),
                    null,
                    null
                )

                return Result.success()
            } catch (e: IOException) {
                val errorMessage = "Error downloading and saving image: ${e.message}"
                return Result.failure(workDataOf("error" to errorMessage))
            }
        }


    }

    companion object {

        const val TAG = "ForegroundWorker"
        const val NOTIFICATION_ID = 42
        const val CHANNEL_ID = "Job progress"

    }
}