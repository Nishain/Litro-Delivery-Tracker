package com.ndds.litrocustomerlogger

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class CustomerInfoPop : AppCompatActivity() {
    private var phoneNumber =  "1234567"//intent.getStringExtra("phone_number")
    private var address = "example address 2"//intent.getStringExtra("address")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_info_pop)
        phoneNumber = intent.getStringExtra("phone_number").toString()
        address = intent.getStringExtra("address").toString()
        findViewById<TextView>(R.id.customerNumberHint).setText(
            "call from ${phoneNumber}"
        )
        findViewById<TextView>(R.id.customerAddressHint).setText(
            "from address ${address}"
        )
    }
    override fun onAttachedToWindow() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
    }
}
