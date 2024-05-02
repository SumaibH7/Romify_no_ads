package com.bluell.roomdecoration.interiordesign.common

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class InspireGenertionDialog   {

    private var dialog: Dialog? = null
    private var imageJob: Job? = null

    fun show(context: Context, list:ArrayList<genericResponseModel?>): Dialog? {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return show(context, null,list)
    }

    fun show(context: Context, title: CharSequence?, list:ArrayList<genericResponseModel?>): Dialog? {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return show(context, title, false,list)
    }

    fun show(context: Context, title: CharSequence?, cancelable: Boolean, list:ArrayList<genericResponseModel?>): Dialog? {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return show(context, title, cancelable, null,list)
    }

    fun show(
        context: Context, title: CharSequence?, cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?,
        list:ArrayList<genericResponseModel?>
    ): Dialog? {
        dialog?.dismiss()
        val inflator = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflator.inflate(R.layout.dialog_generation_inspire, null)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val imageView = view.findViewById<ShapeableImageView>(R.id.genImg)
        val images = list
        Log.e("TAG", "show: "+list.size )
        val dummyList = ArrayList<String>()
        if (list.isEmpty() || list.size <= 2){
            Log.e("TAG", "show: "+list.size )
            dummyList.add("https://plus.unsplash.com/premium_photo-1678752717095-08cd0bd1d7e7?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
            dummyList.add("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?q=80&w=2071&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
            dummyList.add("https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=2158&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
        }

        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val totalProgressTime = 2000L // 2-3 seconds in milliseconds
        val increment = 20 // Update frequency in milliseconds
        val progressMax = 100

        // Start coroutine
        CoroutineScope(Dispatchers.Main).launch {
            var progress = 0
            while (true) {
                progressBar.progress = progress
                progress += 1

                // Reset the progress when it reaches maximum
                if (progress >= progressMax) {
                    progress = 0
                    delay(totalProgressTime)
                }

                delay(increment.toLong())
            }
        }

        var currentIndex = 0
        imageJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (images.isEmpty()){
                    Glide.with(context.applicationContext)
                        .load(dummyList[currentIndex])
                        .placeholder(R.drawable.splash_bg)
                        .error(R.drawable.splash_bg)
                        .into(imageView)

                    delay(1000)

                    currentIndex = (currentIndex + 1) % dummyList.size
                }else{
                    if (images.size > 2){
                        Glide.with(context.applicationContext)
                            .load(images[currentIndex]?.output!![0])
                            .placeholder(R.drawable.splash_bg)
                            .error(R.drawable.splash_bg)
                            .into(imageView)

                        delay(1000)

                        currentIndex = (currentIndex + 1) % images.size
                    }else{
                        Glide.with(context.applicationContext)
                            .load(dummyList[currentIndex])
                            .placeholder(R.drawable.splash_bg)
                            .error(R.drawable.splash_bg)
                            .into(imageView)

                        delay(1000)

                        currentIndex = (currentIndex + 1) % dummyList.size
                    }

                }


            }
        }

        if (dialog==null){
            dialog = Dialog(context, R.style.NewDialog)

        }
        dialog!!.setContentView(view)
        dialog!!.setCancelable(cancelable)
        dialog!!.setOnCancelListener(cancelListener)
        dialog!!.show()
        return dialog
    }

    fun setTextDialog(text:String){
        dialog?.setTitle(text)
    }


    fun dismissDialog() {
        dialog?.dismiss()
        imageJob?.cancel()
    }

    fun getDialog(): Dialog? {
        return dialog
    }
}