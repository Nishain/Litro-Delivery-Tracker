package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONArray
import java.util.*

class CustomerHome : AppCompatActivity() {

    private  lateinit var sharedPreference: SharedPreferences
    private lateinit var primaryPhoneNumber: String
    private var phonumbers: String? = null
    private var listener: ListenerRegistration?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_home)

        val session = if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>12)"Afternoon" else "Morning"
        sharedPreference = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        val name = sharedPreference.getString("username","Stranger")
        if (name != null) {
            findViewById<TextView>(R.id.Welcome_text).setText("Good $session ${name.capitalize()}")
        }
        phonumbers = getSharedPreferences("localStorage",MODE_PRIVATE).getString("phone number",null)
        primaryPhoneNumber = JSONArray(phonumbers).getString(0)
        engageListener()
        findViewById<Button>(R.id.backToMap).setOnClickListener{v->
            startActivityForResult(Intent(this, MapActivity::class.java).putExtra("phoneNumber",primaryPhoneNumber),897)
        }
    }
    private fun engageListener(){
        if(listener!=null)
            return
        listener = FirebaseFirestore.getInstance().document("customer/$primaryPhoneNumber").addSnapshotListener{ value, error->
            if(value!=null && value.exists()) {
                if (value.contains("isAccepted") && value.getBoolean("isAccepted")!!){
                    if(value.getLong("processCode")==1L){
                        listener?.remove()
                        listener = null
                        startActivityForResult(Intent(this, MapActivity::class.java).putExtra("phoneNumber",primaryPhoneNumber),897)
                    }else
                    {
                        FirebaseFirestore.getInstance().document("delivererLocation/$primaryPhoneNumber").delete()
                        FirebaseFirestore.getInstance().document("customer/$primaryPhoneNumber").delete()
                    }

                }
            }else
                findViewById<TextView>(R.id.customerWelcomeTxt).setText(R.string.orderNotProcessed)
        }
    }
    fun editProfile(v: View){
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun onResume() {
        if(sharedPreference.contains("lockScreenPopupState") && sharedPreference.getInt("lockScreenPopupState",0)!=2){
            PopupEngine().askAdditionalPermissions(this,sharedPreference)
        }
        super.onResume()
    }
    fun viewCredits(v:View){
        PopupEngine().showCredit(this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        findViewById<Button>(R.id.backToMap).visibility = if(resultCode==555) View.GONE else View.VISIBLE
        if(resultCode==555){
            FirebaseFirestore.getInstance().document("delivererLocation/$primaryPhoneNumber").delete()
            FirebaseFirestore.getInstance().document("customer/$primaryPhoneNumber").delete().addOnSuccessListener {
                engageListener()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
