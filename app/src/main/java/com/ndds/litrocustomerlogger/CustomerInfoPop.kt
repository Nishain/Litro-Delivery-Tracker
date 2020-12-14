package com.ndds.litrocustomerlogger

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class CustomerInfoPop : AppCompatActivity() {
    private var phoneNumber =  "1234567"//intent.getStringExtra("phone_number")
    private var address = "example address 2"//intent.getStringExtra("address")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_info_pop)
        getSharedPreferences("localStorage", Context.MODE_PRIVATE)
            .edit().putInt("lockScreenPopupState", 2).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        //window.setGravity(Gravity.TOP)
        phoneNumber = intent.getStringExtra("phone_number").toString()
        address = intent.getStringExtra("address").toString()
        findViewById<TextView>(R.id.customerNumberHint).setText(
            "call from ${phoneNumber}"
        )
        findViewById<TextView>(R.id.customerAddressHint).setText(
            "from address ${address}"
        )
    }

}
