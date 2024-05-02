package com.bluell.roomdecoration.interiordesign.common

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.ForegroundWorker.Companion.CHANNEL_ID
import com.bluell.roomdecoration.interiordesign.common.ForegroundWorker.Companion.NOTIFICATION_ID
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class ImageAvailabilityWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    var pendingIntent: PendingIntent? = null
    val preferenceDataStoreHelper = UserPreferencesDataStoreHelper(appContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val imageUrl = inputData.getString(KEY_IMAGE_URL) ?: return@withContext Result.failure()
        val artId = inputData.getString(ART_ID) ?: return@withContext Result.failure()
        if (isImageAvailable(imageUrl)) {
            val value= preferenceDataStoreHelper.getFirstPreference<String>(
                PreferenceDataStoreKeysConstants.WORK_MANAGER_JOB_GENERIC,
                ""
            )
            Log.e("TAG", "doWork: the Value of Prefrence is $value", )
            val arr=value.split("_")
            if (arr.isNotEmpty()){
                if (arr.size>1){
                    if (arr[1]==artId){
                        preferenceDataStoreHelper.removePreference(PreferenceDataStoreKeysConstants.WORK_MANAGER_JOB_GENERIC,)
                    }
                }
            }

            showNotification("Image Available", "The image is now available!",artId)
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag("image_check_tag")

        } else {
            enqueueNextWorkRequest(imageUrl,artId)
        }

        Result.success()
    }

    private suspend fun isImageAvailable(imageUrl: String): Boolean {
        Log.e("TAG", "isImageAvailable: trying")
        var connection: HttpURLConnection? = null
        try {
            val url = URL(imageUrl)
            connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000 // 5 seconds timeout
            Log.e("TAG", "isImageAvailable: trying again")
            val responseCode = connection.responseCode
            return responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            return false
        } finally {
            connection?.disconnect()
        }
    }

    private fun showNotification(title: String, message: String,id:String) {
        Log.e("TAG", "isImageAvailable: notification")
        val bundle = Bundle()
        bundle.putString("id", id)
        pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.main_navigation)
            .setDestination(R.id.cameraXFragment)
            .setArguments(bundle)
            .createPendingIntent()


        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(NOTIFICATION_ID, notification)
        }
    }
    private fun enqueueNextWorkRequest(imageUrl: String,id:String) {
        val inputData = workDataOf(KEY_IMAGE_URL to imageUrl, ART_ID to id)

        val nextWorkRequest = OneTimeWorkRequest.Builder(ImageAvailabilityWorker::class.java)
            .setInputData(inputData)
            .addTag("image_check_tag") // Add a unique tag
            .setInitialDelay(30,TimeUnit.SECONDS) // Initial delay of 2 minutes
            .build()

        WorkManager.getInstance(applicationContext).enqueue(nextWorkRequest)
    }


    companion object {
        const val KEY_IMAGE_URL = "image_url"
        const val ART_ID = "art_id"
    }
}