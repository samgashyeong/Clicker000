package com.example.clicker.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.clicker.R

class DefaultDialog(context: Context, private val uiText : DefaultDialogDto,
                    private val clickListener: () -> Unit,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_default)
        this.setCancelable(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        this.window.let {
//            it?.setGravity(Gravity.BOTTOM)
//            it?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        }

        findViewById<TextView>(R.id.mainTv).text = uiText.mainText
        findViewById<TextView>(R.id.subTv).text = uiText.subText
        findViewById<TextView>(R.id.checkBtn2).text = uiText.checkButton
        findViewById<TextView>(R.id.cancelBtn).text = uiText.cancelButton
        findViewById<TextView>(R.id.checkBtn2).setOnClickListener {
            clickListener.invoke()
            this.cancel()
        }
        findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
            this.cancel()
        }

    }
}