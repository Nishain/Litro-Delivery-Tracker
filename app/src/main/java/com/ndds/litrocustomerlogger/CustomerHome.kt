package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_customer_home.*
import java.util.*

class CustomerHome : AppCompatActivity() {

    private var shouldListenerWork: Boolean = true
    private var phonumber: String? = null
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_home)

        val session = if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>12)"Afternoon" else "Morning"
        val name = getSharedPreferences("localStorage", Context.MODE_PRIVATE).getString("username","Stranger")
        if (name != null) {
            findViewById<TextView>(R.id.Welcome_text).setText("Good $session ${name.capitalize()}")
        }
        phonumber = getSharedPreferences("localStorage",MODE_PRIVATE).getString("phone number",null)
        listener = FirebaseFirestore.getInstance().document("customer/$phonumber").addSnapshotListener{value, error->
            if(value!=null) {
                if (value.contains("isAccepted") && value.getBoolean("isAccepted")!!){
                    listener.remove()
                    startActivityForResult(Intent(this, MapActivity::class.java).putExtra("phoneNumber",phonumber),897)
                }
            }else
                findViewById<TextView>(R.id.customerWelcomeTxt).setText(R.string.orderNotProcessed)
        }
        findViewById<Button>(R.id.backToMap).setOnClickListener{v->
            startActivityForResult(Intent(this, MapActivity::class.java).putExtra("phoneNumber",phonumber),897)
        }
    }
    fun editProfile(v: View){
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        shouldListenerWork=false
        findViewById<Button>(R.id.backToMap).visibility = if(resultCode==555) View.GONE else View.VISIBLE
    }
}
