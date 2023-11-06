package com.joinself.sdk.sample

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ProgressBar
import com.joinself.sdk.sample2.R

object DialogUtils {
    fun showProgressBar(context: Context, cancelable: Boolean = false): Dialog {
        val dialog = Dialog(context, R.style.MyDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress_bar, null)

        dialog.setContentView(view)
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        return dialog
    }
}