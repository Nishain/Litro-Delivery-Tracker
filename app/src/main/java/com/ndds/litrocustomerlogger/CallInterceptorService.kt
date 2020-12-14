package com.ndds.litrocustomerlogger

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


class CallInterceptorService : Service() {
    lateinit var  register:CallStateListener
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null && intent.hasExtra("terminateService")){
            stopForeground(true)
            return START_STICKY
        }
        register = CallStateListener()
        registerReceiver(register, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        val notificationID = "Call Interceptor Service"
        //creating the notification channel...
        val manager = getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationID,
                "Call Interceptor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            if(manager.getNotificationChannel(notificationID)==null)
                manager.createNotificationChannel(serviceChannel)

        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            456, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        var builder:NotificationCompat.Builder
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
             builder =  NotificationCompat.Builder(this, notificationID)
        else
            builder = NotificationCompat.Builder(this)
        val notification: Notification =  builder
            .setSmallIcon(R.drawable.litro_icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notificationBody))
            .addAction(android.R.drawable.ic_delete,"Delete", PendingIntent.getService(this,234,
                Intent(this,CallInterceptorService::class.java)
                    .putExtra("terminateService",true),PendingIntent.FLAG_IMMUTABLE))
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this,R.color.colorAccent))
         //   .addAction(android.R.drawable.ic_delete,"Remove",PendingIntent.getService(this,456,Intent(this,this::class.java),PendingIntent.FLAG_CANCEL_CURRENT))
            .build()
        startForeground(1, notification);
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(register)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
