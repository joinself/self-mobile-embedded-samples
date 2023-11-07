package com.joinself.sdk.sample

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.joinself.sdk.sample.chat.R

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

    fun showMaterialDialog(context: Context,
                           title: String = "", message: CharSequence,
                           positiveButtonTitle: String = "Ok", negativeButtonTitle: String? = null,
                           dialogTheme: Int = R.style.DialogStyle,
                           cancelledOutside: Boolean = true,
                           listener: DialogInterface.OnClickListener,): AlertDialog {
        val contextThemeWrapper = ContextThemeWrapper(context, dialogTheme)
        val alertDialog = AlertDialog.Builder(contextThemeWrapper).create()
//        val alertDialog = MaterialAlertDialogBuilder(contextThemeWrapper).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonTitle, listener)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        negativeButtonTitle?.let {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonTitle, listener)
        }

        alertDialog.setCanceledOnTouchOutside(cancelledOutside)
        alertDialog.setCancelable(cancelledOutside)
        alertDialog.show()
        return alertDialog
    }
}