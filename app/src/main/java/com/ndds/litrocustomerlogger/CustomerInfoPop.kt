package com.ndds.litrocustomerlogger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

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

}
