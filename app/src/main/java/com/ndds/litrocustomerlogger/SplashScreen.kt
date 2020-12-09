package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val sharedPreferences = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        Handler(Looper.getMainLooper()).postDelayed({
            if(!sharedPreferences.contains("isUserCustomer"))
                startActivity(Intent(this,MainActivity::class.java))
            else if(sharedPreferences.getBoolean("isUserCustomer",true))
                startActivity(Intent(this,CustomerHome::class.java))
            else
                startActivity(Intent(this,ServeList::class.java))
            finish()
        },400)

    }
}
