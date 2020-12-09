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
import kotlinx.android.synthetic.main.activity_customer_home.*
import kotlinx.android.synthetic.main.row_layout.*

class ServeList : AppCompatActivity() {

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
                startActivityForResult(
                    Intent(context,DelievererLocationTransmitter::class.java)
                        .putExtra("phoneNumber",phoneNumber).putExtra("address",address),456
                )
            }

            override fun onDataEmpty() {
                findViewById<ViewGroup>(R.id.dataListContainer).visibility = View.GONE
            }
        }
        findViewById<Button>(R.id.backToTaskBtn).setOnClickListener { v->
            startActivityForResult(
                Intent(this,DelievererLocationTransmitter::class.java)
                    .putExtra("phoneNumber",currentPhoneNumber).putExtra("address",currentAddress),456
            )
        }
        findViewById<ListView>(R.id.serveList).adapter = adapter

    }
    fun editProfile(v: View){
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK) {
            findViewById<Button>(R.id.backToTaskBtn).visibility = View.GONE
            val removingPhoneNumber = data?.getStringExtra("removingPhoneNumber")
            if(removingPhoneNumber!=null) {
                for (data in dataList) {
                    if (data.split("<.>")[0] == removingPhoneNumber) {
                        dataList.remove(data)
                        break
                    }
                }
                adapter.refreshData(dataList.toTypedArray())
                findViewById<ListView>(R.id.serveList).adapter = adapter
            }
        }else
            findViewById<Button>(R.id.backToTaskBtn).visibility = View.VISIBLE
    }
}
