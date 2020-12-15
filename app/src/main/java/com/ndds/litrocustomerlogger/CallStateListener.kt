package com.ndds.litrocustomerlogger

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

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
                        Log.d("debug","should start the activity!!")
                        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if(keyguardManager.isKeyguardLocked()) {
                            sharedPreference.edit().putInt("lockScreenPopupState", 1).apply()
                            context.startActivity(
                                Intent(context, CustomerInfoPop::class.java)
                                    .putExtra("phone_number", recievedPhoneNumber)
                                    .putExtra("address", address)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        else
                        popAddressDialog(context,recievedPhoneNumber,address)

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
                            "isAvailable" to true,
                            "simNumbers" to arrayList.toTypedArray().toList()
                        )
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
                .setFullScreenIntent(PendingIntent.getActivity(context,456,Intent(context,CustomerInfoPop::class.java),PendingIntent.FLAG_UPDATE_CURRENT),true)
                .setContentText("You might have received deliverer order from customer").build()
        notificationManager.notify(20,notification)
    }
    private fun popAddressDialog(context:Context, phoneNumber: String, address: String){
        val dialog = AlertDialog.Builder(context).setMessage("Hello world!").create()
        val window = dialog.window!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val viewGroup: ViewGroup = LayoutInflater.from(context).inflate(R.layout.activity_customer_info_pop,null) as ViewGroup
        viewGroup.findViewById<TextView>(R.id.customerNumberHint).text = "call from $phoneNumber"
        viewGroup.findViewById<TextView>(R.id.customerAddressHint).text = "from address $address"
        dialog.setView(viewGroup)
        dialog.show()
    }
}
