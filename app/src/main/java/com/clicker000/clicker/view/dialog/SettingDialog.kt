package com.clicker000.clicker.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.Observer
import com.clicker000.clicker.R
import com.clicker000.clicker.viewmodel.SettingDataStoreViewModel

class SettingDialog(context: Context,
                    val dataStoreViewModel: SettingDataStoreViewModel,
) : Dialog(context) {

    private val lifeCycleOwner: MyLifeCycleOwner by lazy { MyLifeCycleOwner() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_setting)
        this.setCancelable(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnInversionButton = findViewById<CheckBox>(R.id.btnInversionButton)
        val vibrateButton = findViewById<CheckBox>(R.id.vibrateButton)


        dataStoreViewModel.isChagneButton.observe(lifeCycleOwner, Observer {
            btnInversionButton.isChecked = it ?: false
        })

        dataStoreViewModel.isVibButton.observe(lifeCycleOwner, Observer {
            vibrateButton.isChecked = it ?: false
        })

        btnInversionButton.setOnCheckedChangeListener { compoundButton, b ->
            //데이터 관련코드
            dataStoreViewModel.saveIsChangeButton(b)
        }
        vibrateButton.setOnCheckedChangeListener { compoundButton, b ->
            //데이터 코드
            dataStoreViewModel.saveIsvibButton(b)
        }
        findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
            this.cancel()
        }

    }
}