package com.bluell.roomdecoration.interiordesign.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.bluell.roomdecoration.interiordesign.R


class CustomProgressBar {

    private var dialog: AlertDialog? = null

    private fun createDialog(context: Context,text:String): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_loading, null)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.setLayoutParams(layoutParams)
        builder.setView(view)
        val textViewTitle = view?.findViewById<TextView>(R.id.title)

        textViewTitle?.text = text

        return builder.create()
    }

    fun show(context: Context, title: String, cancelable: Boolean = false): AlertDialog {
        dialog?.dismiss() // Dismiss existing dialog if any
        dialog = createDialog(context, text = title)

        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.setCancelable(cancelable)
        dialog?.show()
        return dialog!!
    }

    fun setTextDialog(text: String) {
        dialog?.setTitle(text)
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun getDialog(): AlertDialog? {
        return dialog
    }
}
