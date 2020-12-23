package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this,CallInterceptorService::class.java))
        }else
            startService(Intent(this,CallInterceptorService::class.java))
        val sharedPreferences = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        Handler(Looper.getMainLooper()).postDelayed({
            if(!sharedPreferences.contains("isUserCustomer"))
                startActivity(Intent(this,MainActivity::class.java))
            else if(sharedPreferences.getBoolean("isUserCustomer",true))
                startActivity(Intent(this,CustomerHome::class.java))
            else
                startActivity(Intent(this,DelivererHome::class.java))
            finish()
        },1500)

    }
}
