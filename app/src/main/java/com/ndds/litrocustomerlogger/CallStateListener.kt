package com.ndds.litrocustomerlogger

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class CallStateListener() : BroadcastReceiver() {
    val db = FirebaseFirestore.getInstance()
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreference = context.getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        val phonenumber = sharedPreference.getString("phone number",null)
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
                    db.document("customer/${recievedPhoneNumber}").get().addOnSuccessListener { result ->
                        if(!result.contains("address"))
                            return@addOnSuccessListener
                        addRequest(sharedPreference,recievedPhoneNumber,
                            result.getString("address")!!
                        )
                        Log.d("debug","should start the activity!!")
                        popAddressDialog(context,recievedPhoneNumber,"somewhere")

                        /*context.startActivity(
                            Intent(context,CustomerInfoPop::class.java).
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP).
                            putExtra("phone_number",recievedPhoneNumber)
                                .putExtra("address", result.getString("address")!!))*/
                        Log.d("debug", result.get("address").toString())

                    }
                }

            }


            TelephonyManager.EXTRA_STATE_OFFHOOK->{
                    val outGoingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d("debug","off rook ran")
                if(isUserCustomer && outGoingNumber!=null && !didRang) { // this is outgoing call
                    Log.d("debug","update database")
                    db.document("customer/${phonenumber}").set(
                        hashMapOf(
                            "address" to address,
                            "isAvailable" to true
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
