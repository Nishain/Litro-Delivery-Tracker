package com.ndds.litrocustomerlogger

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat


class CallInterceptorService : Service() {
    lateinit var  register:CallStateListener
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        register = CallStateListener()
        registerReceiver(register, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        val notificationID = "Call Interceptor Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationID,
                "Call Interceptor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager =
                getSystemService(NotificationManager::class.java)
            if(manager.getNotificationChannel(notificationID)==null)
                manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            456, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, notificationID)
            .setContentTitle("Blah")
            .setContentText("As long this is visble your calls will be intercepted")
            .setContentIntent(pendingIntent)
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
