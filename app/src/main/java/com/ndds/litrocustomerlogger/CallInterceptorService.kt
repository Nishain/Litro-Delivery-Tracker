package com.ndds.litrocustomerlogger

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class CallInterceptorService : Service() {
    private lateinit var deliveryStatusListener: ListenerRegistration
    lateinit var  register:CallStateListener
    val notificationID = "backgroundNotifications"
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null && intent.hasExtra("terminateService")){
            stopSelf()
            return START_NOT_STICKY
        }

        register = CallStateListener()
        registerReceiver(register, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))


        //creating the notification channel...
        val manager = getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationID,
                "background Notifications",
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
            .setSmallIcon(R.drawable.gas_cylinder)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notificationBody))
            .addAction(android.R.drawable.ic_delete,"Stop Task", PendingIntent.getService(this,234,
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
        deliveryStatusListener.remove()
        unregisterReceiver(register)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? {
        if(intent!=null && intent.hasExtra("startListeningCompletionStatus")){
            deliveryStatusListener = FirebaseFirestore.getInstance()
                .document("customer/${intent.getStringExtra("phoneNumber")}")
                .addSnapshotListener{snapshot, error ->
                    if(!snapshot?.exists()!!) {
                        deliveryStatusListener.remove()
                        return@addSnapshotListener
                    }
                    Log.d("debug","service snapshot listener called")
                    var isDeliverySuccessfull = false
                    if(!snapshot.contains("processCode") || (snapshot.get("processCode") as Long) == 1L)
                        return@addSnapshotListener
                    if (snapshot.get("processCode")!! as Long == 0L) {
                        isDeliverySuccessfull = false
                    }
                    else if (snapshot.get("processCode")!! as Long == 2L) {
                        isDeliverySuccessfull = true
                    } else if((snapshot.get("processCode") as Long) == 1L)
                        return@addSnapshotListener
                    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    val message = getString(if(isDeliverySuccessfull) R.string.CustomerSuccessMessage else R.string.deliveryCancel)
                    val imageID = if(isDeliverySuccessfull)R.drawable.ic_check_circle_black_24dp else R.drawable.ic_error_black_24dp

                    val manager = getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
                    var builder: NotificationCompat.Builder
                    if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
                        builder =  NotificationCompat.Builder(this, notificationID)
                    else
                        builder = NotificationCompat.Builder(this)
                    val notification: Notification =  builder.setPriority(Notification.PRIORITY_HIGH)
                        .setContentTitle("Your Delivery status has updated")
                        .setSmallIcon(R.drawable.gas_cylinder)
                        .setContentText(message).build()
                    manager.notify(4,notification)

                    if(keyguardManager.isKeyguardLocked()) {
                        //if the screen is locked show and activity
                        val sharedPreference = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
                        val popActivityIntent = Intent(this, LockScreenPopup::class.java)
                            .putExtra("isUserCustomer", true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("completionMessage",message)
                            .putExtra("messageIcon",imageID)
                        sharedPreference.edit().putInt("lockScreenPopupState", 1).apply()
                        Log.d("debug","deice locked procced to open activity")
                        startActivity(
                            popActivityIntent
                        )
                    }else
                        PopupEngine().popDelieveryCompletionAlert(this,message,imageID)
                    return@addSnapshotListener
                }
            return null
        }
        return null
    }
}
