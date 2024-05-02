package com.bluell.roomdecoration.interiordesign

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ImageLoadingHelper {
    companion object {
        private const val TAG = "ImageLoadingHelper"
        private const val RETRY_DELAY_MILLIS = 3000L // Adjust retry delay (milliseconds)
    }

    fun loadImage(
        progressBar: ProgressBar,
        imageView: ImageView,
        imageUrl: String,
        buttons: ConstraintLayout? = null,
        openImage: ConstraintLayout? = null,
        favorites: ImageView? = null,
        share: ImageView? = null,
        save: MaterialButton? = null,
    ) {
        progressBar.visibility = View.VISIBLE

        Glide.with(imageView.context.applicationContext) // Use context from imageView
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Check if the error is recoverable (e.g., network issue)
                    return if (e?.rootCauses?.isNotEmpty() == true && isRecoverableError(e.rootCauses[0])) {
                        // Retry loading image
                        retryLoading(progressBar, imageView, imageUrl)
                        true // Returning true indicates that we've handled the error.
                    } else {
                        // Error is not recoverable, stop retrying
                        progressBar.visibility = View.GONE
//                        Log.d(TAG, "onLoadFailed: $e")
                        false // Returning false indicates that Glide should handle the error.
                    }
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image loaded successfully, hide progress bar
                    progressBar.visibility = View.GONE
                    buttons?.visibility = View.VISIBLE
                    favorites?.visibility = View.VISIBLE
                    save?.visibility = View.VISIBLE
                    openImage?.visibility = View.VISIBLE
                    share?.visibility = View.VISIBLE
                    imageView.setImageDrawable(resource)
                    return false
                }
            })
            .into(imageView)
    }

    private fun retryLoading(progressBar: ProgressBar, imageView: ImageView, imageUrl: String) {
        // Retry loading image after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            loadImage(progressBar, imageView, imageUrl)
        }, RETRY_DELAY_MILLIS)
    }

    private fun isRecoverableError(throwable: Throwable): Boolean {
        // Check if the error is recoverable (e.g., network issue)
        return throwable is IOException || throwable is SocketTimeoutException || throwable is ConnectException
    }
}
