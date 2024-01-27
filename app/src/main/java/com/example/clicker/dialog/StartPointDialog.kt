package com.example.clicker.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.clicker.MainViewModel
import com.example.clicker.R

class StartPointDialog(context: Context,
                       private val viewModel: MainViewModel,
                       ) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_start_point)
        this.setCancelable(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window.let {
            it?.setGravity(Gravity.BOTTOM)
            it?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }


        val startPoint = findViewById<EditText>(R.id.startPointEditText)
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            viewModel.startPoint?.value = startPoint.text.toString().toFloat()
            this.cancel()
        }


    }
}