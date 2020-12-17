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
        val isUserCustomer = intent.getBooleanExtra("isUserCustomer",false)
        setContentView(if(isUserCustomer)R.id.completionMessage else R.layout.activity_customer_info_pop)
        getSharedPreferences("localStorage", Context.MODE_PRIVATE)
            .edit().putInt("lockScreenPopupState", 2).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if(isUserCustomer)
                setTurnScreenOn(true)
            setShowWhenLocked(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or if(isUserCustomer) WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON else 0
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if(isUserCustomer){
            val messageType = intent.getIntExtra("messageType",0)
            findViewById<TextView>(R.id.completionMessage).setText(if(messageType==1)
                R.string.CustomerSuccessMessage
            else
                R.string.deliveryCancel)
            //completionMessage
        }else{
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

}
