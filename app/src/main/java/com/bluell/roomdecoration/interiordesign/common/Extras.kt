package com.bluell.roomdecoration.interiordesign.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object Extras {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        var file: File? = null
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment}"
                file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }


    fun shareApp(context:Context){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, "Check out this cool app: " +
                    "https://play.google.com/store/apps/details?id=" + context.packageName
        )
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    fun rateUs(context: Context) {
        try {
            val uri = Uri.parse("market://details?id=" + context.packageName)
            val rateIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
            val rateIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(rateIntent)
        }
    }


    fun openPrivacyPolicyInBrowser(context: Context) {
        val privacyPolicyUrl =
            "https://swedebras.blogspot.com/2023/09/privacy-policy.html" // Replace with your actual privacy policy URL
        val uri = Uri.parse(privacyPolicyUrl)
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }catch (e:ActivityNotFoundException){
            val rateIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(rateIntent)
        }

    }


    fun moreApps(context: Context){
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LINK_TO_GP))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}