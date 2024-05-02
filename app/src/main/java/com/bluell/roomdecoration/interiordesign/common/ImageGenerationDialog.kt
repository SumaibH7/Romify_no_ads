    package com.bluell.roomdecoration.interiordesign.common

    import android.app.Dialog
    import android.content.Context
    import android.graphics.Color
    import android.graphics.drawable.AnimationDrawable
    import android.graphics.drawable.ColorDrawable
    import android.os.CountDownTimer
    import android.view.LayoutInflater
    import android.view.View
    import android.view.WindowManager
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.appcompat.app.AlertDialog
    import androidx.lifecycle.lifecycleScope
    import com.bluell.roomdecoration.interiordesign.R
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.textview.MaterialTextView
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.isActive
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import javax.inject.Inject
    import javax.inject.Singleton

    @Singleton
    class ImageGenerationDialog @Inject constructor() {

        private var dialog: Dialog? = null
        private var countdownTimer: CountDownTimer? = null
        private lateinit var loadingAnimDrawable: AnimationDrawable
        private lateinit var loaderText: TextView
        private lateinit var cancel: ImageView
        private lateinit var contextThis: Context

        fun show(
            context: Context
        ): Dialog? {
            contextThis = context
            dialog?.dismiss()
            dialog = Dialog(context, R.style.FullScreenDialog)
            val view = LayoutInflater.from(context).inflate(R.layout.generation_dialoh, null)
            dialog?.setContentView(view)
            val loadingAnim = view.findViewById<ImageView>(R.id.loading_anim)
            val progressText = view.findViewById<TextView>(R.id.guideTxt)
            loaderText = view.findViewById(R.id.loaderText)

            loadingAnim.apply {
                setBackgroundResource(R.drawable.loading_animation)
                loadingAnimDrawable = background as AnimationDrawable
                loadingAnimDrawable.start()
            }

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.setCancelable(false)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog!!.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog!!.window!!.attributes = lp
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog?.window?.setDimAmount(0.8f) // Adjust the dim amount as needed

            dialog!!.setCancelable(false)
            dialog!!.show()

            cancel.setOnClickListener {
                dismissDialog()
            }
            CoroutineScope(Dispatchers.IO).launch {
                var count = 0
                while (isActive) {
                    count++
                    delay(1000L)
                    withContext(Dispatchers.Main) {
                        val text = progressText.text.toString()

                        val updatedText: String = if (text.endsWith("...")) {
                            "Processing"
                        } else {
                            "$text."
                        }
                        progressText.text = updatedText
                    }
                }
            }
            // Start countdown timer in a separate function with a default duration (modify as needed)
            startCountdownTimer()

            return dialog
        }

        fun isDialogShowing(): Boolean {
            return dialog?.isShowing ?: false
        }

        private fun startCountdownTimer() {
            val countdownInMillis = 30000L // Set a default countdown duration (30 seconds)
            countdownTimer = object : CountDownTimer(countdownInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    cancel.visibility = View.VISIBLE
                    loaderText.text = "When too many user process in same time, waiting time may exceed the expected time!"
                }
            }.start()
        }

        fun dismissDialog() {
            countdownTimer?.cancel()
            dialog?.dismiss()
        }

        fun getDialog(): Dialog? {
            return dialog
        }
    }
