package com.example.clicker.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.clicker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class EditTextDialog(context: Context, private val uiText : EditTextDialogDto,
                     private val clickListener: (editTextString : String) -> Unit,
                       ) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_start_point)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window.let {
            it?.setGravity(Gravity.BOTTOM)
            it?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val editText = findViewById<EditText>(R.id.subEditText)
        editText.hint = uiText.subText

        findViewById<TextView>(R.id.mainTextView).text = uiText.mainText
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            clickListener.invoke(editText.text.toString())
            this.cancel()
        }

    }
}