package com.example.clicker.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.example.clicker.R

class SettingDialog(context: Context,
                    private val clickListener: () -> Unit,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_setting)
        this.setCancelable(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        findViewById<CheckBox>(R.id.btnInversionButton).setOnCheckedChangeListener { compoundButton, b ->
            //데이터 관련코드
            clickListener.invoke()
            this.cancel()
        }
        findViewById<CheckBox>(R.id.vibrateButton).setOnCheckedChangeListener { compoundButton, b ->
            //데이터 코드
        }
        findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
            this.cancel()
        }

    }
}