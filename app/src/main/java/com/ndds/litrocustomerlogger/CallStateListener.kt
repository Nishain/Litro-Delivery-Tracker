package com.ndds.litrocustomerlogger

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray

class CallStateListener() : BroadcastReceiver() {
    val db = FirebaseFirestore.getInstance()
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreference = context.getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        val phonenumbers = sharedPreference.getString("phone number",null)
        val isUserCustomer = sharedPreference.getBoolean("isUserCustomer",true)
        val address = if(isUserCustomer)sharedPreference.getString("address",null) else null
        var didRang = sharedPreference.getBoolean("didRang",false)
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state){
            TelephonyManager.EXTRA_STATE_RINGING-> {
                Log.d("debug","ringing ran")
                val recievedPhoneNumber: String? = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (!isUserCustomer && recievedPhoneNumber!=null) {
                    Log.d("debug","start lookup")
                    sharedPreference.edit().putBoolean("didRang",true).commit()
                    Log.d("the phone",recievedPhoneNumber)
                    db.collection("customer").whereArrayContains("simNumbers",recievedPhoneNumber).limit(1).get().addOnSuccessListener { result ->
                        if(result.isEmpty() || !result.documents[0].contains("address"))
                            return@addOnSuccessListener
                        val document = result.documents[0]
                        val address = document.getString("address")
                        addRequest(sharedPreference,recievedPhoneNumber,
                            address!!
                        )
                        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if(keyguardManager.isKeyguardLocked()) {
                            sharedPreference.edit().putInt("lockScreenPopupState", 1).apply()
                            context.startActivity(
                                Intent(context, LockScreenPopup::class.java)
                                    .putExtra("phone_number", recievedPhoneNumber)
                                    .putExtra("address", address)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        else
                         PopupEngine().popAlertDialog(context,recievedPhoneNumber,address)

                        /*context.startActivity(
                            Intent(context,CustomerInfoPop::class.java).
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP).
                            putExtra("phone_number",recievedPhoneNumber)
                                .putExtra("address", result.getString("address")!!))*/
                        Log.d("debug",address)

                    }
                }
            }


            TelephonyManager.EXTRA_STATE_OFFHOOK->{
                    val outGoingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d("debug","off rook ran")
                if(isUserCustomer && outGoingNumber!=null && !didRang) { // this is outgoing call
                    Log.d("debug","update database - $phonenumbers")
                    val JSONphoneNumbers = JSONArray(phonenumbers)
                    val arrayList = ArrayList<String>()
                    for (i in 0 until JSONphoneNumbers.length())
                        arrayList.add(JSONphoneNumbers.getString(i))
                    val phoneNumberID = JSONphoneNumbers.getString(0)!!

                    db.document("customer/$phoneNumberID").set(
                        hashMapOf(
                            "address" to address,
                            "simNumbers" to arrayList.toTypedArray().toList(),
                            "isAvailable" to true
                        )
                    )
                    db.document("delivererLocation/$phoneNumberID").set(
                        HashMap<String,String>()
                    )
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE->{
                sharedPreference.edit().putBoolean("didRang",false).commit()
            }
        }
    }
    fun addRequest(sharedPreference:SharedPreferences,phoneNumber:String,address:String){
       // var sharedPreference = congetSharedPreferences("localStorage", AppCompatActivity.MODE_PRIVATE);
        var serveList:MutableSet<String?>? = sharedPreference.getStringSet("serveList",null)
        if(serveList == null)
            serveList =  mutableSetOf()
        serveList.add("${phoneNumber}<.>${address}")
        sharedPreference.edit().putStringSet("serveList",serveList).commit()
    }
    private fun popFullScreenNotification(context: Context){
        val notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        var builder: NotificationCompat.Builder
        val notificationID = "Call Interceptor Service"
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            builder =  NotificationCompat.Builder(context, notificationID)
        else
            builder = NotificationCompat.Builder(context)
            val notification = builder
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.litro_icon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setFullScreenIntent(PendingIntent.getActivity(context,456,Intent(context,LockScreenPopup::class.java),PendingIntent.FLAG_UPDATE_CURRENT),true)
                .setContentText("You might have received deliverer order from customer").build()
        notificationManager.notify(20,notification)
    }

}
