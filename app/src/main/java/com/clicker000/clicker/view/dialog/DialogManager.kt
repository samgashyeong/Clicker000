package com.clicker000.clicker.view.dialog

import android.app.Dialog
import javax.inject.Inject

class DialogManager @Inject constructor(){
    val dialogs : ArrayList<Dialog> = ArrayList()
    fun closeAllDialog(){
        dialogs.forEach { dialog ->
            if (dialog.isShowing) {
                dialog.cancel()
            }
        }
    }
}