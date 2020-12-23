package com.ndds.litrocustomerlogger

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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

class DelivererHome : AppCompatActivity() {

    private lateinit var launchIntent: Intent
    private lateinit var dataList: MutableSet<String>
    private lateinit var adapter: CustomArrayAdapter
    private lateinit var currentPhoneNumber:String
    private lateinit var currentAddress:String
    private lateinit var sharedPreference:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliverer_home)
        sharedPreference = getSharedPreferences("localStorage", MODE_PRIVATE);

       dataList= sharedPreference.getStringSet("serveList", mutableSetOf<String>())!!
        dataList.add("0770665281<.>Galle")
        dataList.add("0770665281<.>Colobo")
        dataList.add("0770665281<.>Kandy")
        dataList.add("0770665281<.>habanthota")
        dataList.add("0770665281<.>kagalle")
        dataList.add("0770665281<.>maharagama")
        dataList.add("0770665281<.>nugegode")
        dataList.add("0770665281<.>botagavia")
        dataList.add("0770665281<.>jaffna")
        val username = sharedPreference.getString("username","stranger")
        findViewById<TextView>(R.id.welcomeHint).setText("Welcome ${username?.capitalize()}")
        adapter  = object:CustomArrayAdapter(this,R.layout.row_layout,dataList.toMutableList()){
            override fun onNavigateToMap(phoneNumber: String,address:String) {
                currentPhoneNumber = phoneNumber
                currentAddress = address
                FirebaseFirestore.getInstance().collection("customer").whereArrayContains("simNumbers",phoneNumber).limit(1).get().addOnSuccessListener { result ->
                    if(!result.isEmpty) {
                        Log.d("debug","the phonenumber ID ${result.documents[0].id}")
                        launchIntent = Intent(context,DelievererLocationTransmitter::class.java)
                            .putExtra("phoneNumber",result.documents[0].id)
                            .putExtra("address",address)
                            .putExtra("callingPhoneNumber",phoneNumber)
                        FirebaseFirestore.getInstance().document("customer/${result.documents[0].id}").update(
                            HashMap<String,Any>().apply {
                                put("isAccepted",true)
                                put("processCode",1)
                            }
                        ).addOnSuccessListener {
                            Toast.makeText(context,"Delivery successfully accepted",Toast.LENGTH_SHORT).show()
                            startActivityForResult(launchIntent,456)
                        }

                    }else
                        Toast.makeText(this@DelivererHome,"Could not find customer phone number in system!",Toast.LENGTH_LONG).show()
                }

            }

            override fun onRemoveItem(removedItem: String) {
                dataList.remove(removedItem)
                sharedPreference.edit().putStringSet("serveList",dataList).apply()
            }

            override fun onDataEmpty() {
                findViewById<ViewGroup>(R.id.dataListContainer).visibility = View.GONE
            }
        }
        findViewById<ListView>(R.id.serveList).adapter = adapter
        findViewById<Button>(R.id.backToTaskBtn).setOnClickListener { v->
            startActivityForResult(
                getLaunchIntent(),456
            )
        }

    }
    fun viewCredits(v:View){
        PopupEngine().showCredit(this)
    }
    fun getLaunchIntent():Intent{
        return launchIntent
    }
    fun editProfile(v: View){
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onResume() {

        if(sharedPreference.contains("lockScreenPopupState") && sharedPreference.getInt("lockScreenPopupState",0)!=2){
            PopupEngine().askAdditionalPermissions(this,sharedPreference)
        }
        super.onResume()
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
                adapter.refreshData(dataList.toMutableList())
                findViewById<ListView>(R.id.serveList).adapter = adapter
        }else
            findViewById<Button>(R.id.backToTaskBtn).visibility = View.VISIBLE
    }
}
