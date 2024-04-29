package com.example.clicker.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.clicker.R

class SaveDialog(
    context: Context, private val initializeDialog: DefaultDialog, private val saveDataDialog: DefaultDialog,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_save_setting)
        this.setCancelable(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        findViewById<ImageView>(R.id.saveButton).setOnClickListener {
            saveDataDialog.show()
        }
        findViewById<ImageView>(R.id.InitializeButton).setOnClickListener {
            initializeDialog.show()
        }
        findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
            this.cancel()
        }

    }
}