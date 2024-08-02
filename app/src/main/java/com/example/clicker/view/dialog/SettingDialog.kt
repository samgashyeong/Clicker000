package com.example.clicker.view.dialog

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.clicker.R
import com.example.clicker.util.intToMode
import com.example.clicker.util.modeToInt
import com.example.clicker.viewmodel.SettingDataStoreViewModel


class SettingDialog(
    context: Context,
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
        val spinner = findViewById<Spinner>(R.id.spinner)


        dataStoreViewModel.isChangeButton.observe(lifeCycleOwner, Observer {
            btnInversionButton.isChecked = it ?: false
        })

        dataStoreViewModel.isVibButton.observe(lifeCycleOwner, Observer {
            vibrateButton.isChecked = it ?: false
        })

        dataStoreViewModel.mode.observe(lifeCycleOwner, Observer {
            spinner.setSelection((modeToInt[it] ?: 0) as Int)
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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // 선택된 아이템의 값을 가져옵니다
                val selectedItem = parent.getItemAtPosition(position).toString()
                // 이벤트 처리
                Log.d(TAG, "onItemSelected: ${position}")
                dataStoreViewModel.saveMode(intToMode[position]!!)
                // 필요한 경우 여기에 추가적인 로직을 추가하세요
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }
}