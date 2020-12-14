package com.ndds.litrocustomerlogger

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_customer_home.*
import kotlinx.android.synthetic.main.row_layout.*

class ServeList : AppCompatActivity() {

    private lateinit var launchIntent: Intent
    private lateinit var dataList: MutableSet<String>
    private lateinit var adapter: CustomArrayAdapter
    private lateinit var currentPhoneNumber:String
    private lateinit var currentAddress:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serve_list)
        var sharedPreference = getSharedPreferences("localStorage", MODE_PRIVATE);
       dataList= sharedPreference.getStringSet("serveList", mutableSetOf<String>())!!
        dataList.add("0770665281<.>elliotroad")
        dataList.add("0770665251<.>elliotroad")
        val username = sharedPreference.getString("username","stranger")
        findViewById<TextView>(R.id.welcomeHint).setText("Welcome ${username?.capitalize()}")
        adapter  = object:CustomArrayAdapter(this,R.layout.row_layout,dataList.toTypedArray()){
            override fun onNavigateToMap(phoneNumber: String,address:String) {
                currentPhoneNumber = phoneNumber
                currentAddress = address
                FirebaseFirestore.getInstance().collection("customer").whereArrayContains("simNumbers",phoneNumber).limit(1).get().addOnSuccessListener { result ->
                    if(!result.isEmpty) {
                        launchIntent = Intent(context,DelievererLocationTransmitter::class.java)
                            .putExtra("phoneNumber",result.documents[0].id)
                            .putExtra("address",address)
                            .putExtra("callingPhoneNumber",phoneNumber)
                        startActivityForResult(launchIntent,456)
                    }else
                        Toast.makeText(this@ServeList,"Could not find customer phone number in system!",Toast.LENGTH_LONG).show()
                }

            }

            override fun onRemoveItem(removedItem: String) {
                for(d in dataList){
                    if(d==removedItem){
                        dataList.remove(d)
                        sharedPreference.edit().putStringSet("serveList",dataList).apply()
                        break
                    }
                }
            }

            override fun onDataEmpty() {
                findViewById<ViewGroup>(R.id.dataListContainer).visibility = View.GONE
            }
        }
        findViewById<Button>(R.id.backToTaskBtn).setOnClickListener { v->
            startActivityForResult(
                getLaunchIntent(),456
            )
        }
        findViewById<ListView>(R.id.serveList).adapter = adapter
    }
    fun getLaunchIntent():Intent{
        return launchIntent
    }
    fun editProfile(v: View){
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK) {
            findViewById<Button>(R.id.backToTaskBtn).visibility = View.GONE
                for (data in dataList) {
                    if (data.split("<.>")[0] == currentPhoneNumber) {
                        dataList.remove(data)
                        break
                    }
                }
                adapter.refreshData(dataList.toTypedArray())
                findViewById<ListView>(R.id.serveList).adapter = adapter
        }else
            findViewById<Button>(R.id.backToTaskBtn).visibility = View.VISIBLE
    }
}
