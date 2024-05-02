package com.bluell.roomdecoration.interiordesign.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.bluell.roomdecoration.interiordesign.MainActivity
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.UpscaleResponseDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.webHookGenericResponse
import com.bluell.roomdecoration.interiordesign.data.models.response.webHookResponseUpscale
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "MyFirebaseMsgService"
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        val preferenceDataStoreHelper = UserPreferencesDataStoreHelper(applicationContext)
        GlobalScope.launch {
            preferenceDataStoreHelper.putPreference(
                PreferenceDataStoreKeysConstants.FIREBASE_TOKEN,
                token
            )
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val appDatabase = AppDatabase(applicationContext)
        val data = remoteMessage.data
        Log.e(TAG, "onMessageReceived: $data")
        val type = data["type"]

        if (type?.lowercase() == "a"){
            val body = data["body"]
            val webHookGenericResponse = Gson().fromJson(body, webHookGenericResponse::class.java)
//            Log.e(TAG, "onMessageReceived: $webHookGenericResponse")
            val oldData = appDatabase.genericResponseDao()?.getCreationsByIdNotLive(webHookGenericResponse.id)
            val mutableLIst: MutableList<String> = mutableListOf()
            mutableLIst.addAll(webHookGenericResponse.output)
            oldData?.output = mutableLIst
            if (oldData!=null){
                appDatabase.genericResponseDao()?.UpdateData(oldData)
                showClickableNotification("Design has been generated!", oldData)
            }
//            Log.e(TAG, "onMessageReceivedA: $body")
        }else{
            val body = data["body"]
//            Log.e(TAG, "onMessageReceivedB: $body")
            val webHookGenericResponse = Gson().fromJson(body, webHookResponseUpscale::class.java)
            val genericResponseModel = appDatabase.upscaleDao()?.getCreationsByIdNotLiveMessage(webHookGenericResponse.id)
            if (genericResponseModel!=null){
                genericResponseModel.output = webHookGenericResponse.output[0]
//                Log.e(TAG, "onMessageReceivedB: "+webHookGenericResponse.output[0])
                genericResponseModel.let { appDatabase.upscaleDao()?.updateOutputById(it.id,webHookGenericResponse.output[0], it.arrayID) }
            }
            showNotification("Image has been upscaled!", "webHookGenericResponse")
        }

    }

    private fun showClickableNotification(title: String?, response: genericResponseModel) {
        Log.d(TAG, "showClickableNotification: ${response.id}")
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("art", Gson().toJson(response)) // Add extra to skip splash screen
            putExtra("id", response.id) // Add extra to navigate to HomeFragment
        }

        // Create a PendingIntent for MainActivity
        val mainActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(this, "Job progress")
            .setContentTitle(title)
            .setContentText("Your Art has been generated successfully!")
            .setContentIntent(mainActivityPendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.app_icon) // Replace with your app's notification icon

        val notificationManager = NotificationManagerCompat.from(this)

        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Job progress",
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(42, notificationBuilder.build())
    }

    private fun showNotification(title: String?, text:String) {
        val notificationBuilder = NotificationCompat.Builder(this, "Job progress")
            .setContentTitle(title)
            .setContentText("Your Art has been generated successfully!")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.app_icon) // Replace with your app's notification icon

        val notificationManager = NotificationManagerCompat.from(this)

        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Job progress",
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(42, notificationBuilder.build())
    }
}
