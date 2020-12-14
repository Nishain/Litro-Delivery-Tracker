package com.ndds.litrocustomerlogger

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity()  {
    private var isUserCustomer: Boolean = false
    val customerFields:HashMap<Int,String> = hashMapOf(
        R.id.username to "username",
       // R.id.phone_number to "phone number",
        R.id.address to "address"
    )
    val delivererFields:HashMap<Int,String> = hashMapOf(
        R.id.username to "username"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initValues()
        requestPermission()

        findViewById<RadioGroup>(R.id.userType).setOnCheckedChangeListener{group,checkedID->
            val isCustomer = checkedID==R.id.customer
            findViewById<ViewGroup>(R.id.customerControls).visibility = if(isCustomer) View.VISIBLE else View.GONE
        }
        findViewById<Button>(R.id.addSimNumberBtn).setOnClickListener{v->
            val phoneNumberGroup = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)
            addSimPhoneNumber(phoneNumberGroup,"")
        }
        /*(getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                super.onCallStateChanged(state, incomingNumber)
                println("incomingNumber : $incomingNumber")
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)*/
    }
    fun test(v:View){
        startActivity(Intent(this,CustomerInfoPop::class.java))
    }
    fun test2(v:View){
        startActivity(Intent(this,ServeList::class.java))
    }
    fun navigateToMainScreen(){
        startActivity(
            Intent(this,
                if(isUserCustomer)CustomerHome::class.java else ServeList::class.java))
    }

    fun home(v:View){
        navigateToMainScreen()
    }
    @SuppressLint("MissingPermission")
    fun saveNumber(v: View){
        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        var editText:EditText

        val isUserCustomer = findViewById<RadioGroup>(R.id.userType).checkedRadioButtonId==R.id.customer
        val simNumbers = ArrayList<String>()
        if(isUserCustomer){
            val viewGroup = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)
            for (child in viewGroup.children){
                val number = child.findViewById<EditText>(R.id.phone_number).text.toString()
                if(number.isEmpty()){
                    showEmptyFieldError()
                    return
                }
                simNumbers.add(number)
            }

            storage.edit().putString("phone number",simNumbers.toTypedArray().contentToString()).apply()
        }
        val fields = if(isUserCustomer) customerFields else delivererFields
        for (e in fields.entries){
            editText = findViewById(e.key)
            if(editText.text.toString().isEmpty()){
                showEmptyFieldError()
                return
            }

            storage.edit().putString(e.value,editText.text.toString()).apply()
        }
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionInfoList =
                getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            val subscriptionInformations = subscriptionInfoList.getActiveSubscriptionInfoList()

            if (subscriptionInformations != null && subscriptionInformations.size > 0) {
                for (info in subscriptionInformations) {
                    info.simSlotIndex
                    val carrierName = info.carrierName.toString()
                    val mobileNo = info.number
                    val countyIso = info.countryIso
                    val dataRoaming = info.dataRoaming
                }
            }
        }*/
        storage.edit().putBoolean("isUserCustomer",isUserCustomer).apply()
        Toast.makeText(this,"saved settings!",Toast.LENGTH_SHORT).show()
        navigateToMainScreen()
        finish()
    }
    fun showEmptyFieldError(){
        AlertDialog.Builder(this).setMessage("Some of the fields are empty.Please fill them before continue!")
            .setTitle("Some of the fields are empty")
            .setIcon(R.drawable.error).show()
    }
    fun addSimPhoneNumber(phoneNumberContainer:ViewGroup, initialNumber:String){
        val simRow = layoutInflater.inflate(R.layout.sim_phonenumber_row,null) as ViewGroup
        phoneNumberContainer.addView(simRow)
        simRow.findViewById<EditText>(R.id.phone_number).setText(initialNumber)
        if(phoneNumberContainer.childCount==1)
            simRow.findViewById<View>(R.id.removeNumber).visibility = View.GONE
        else{
            simRow.findViewById<View>(R.id.removeNumber).setOnClickListener { v ->
                phoneNumberContainer.removeView(v.parent as View)//v.getTag(R.id.POSITION_KEY) as Int
            }
        }
    }
    fun initValues(){

        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        isUserCustomer = storage.getBoolean("isUserCustomer",true)
        findViewById<RadioGroup>(R.id.userType).check(if(isUserCustomer)R.id.customer else R.id.deliverer)
        if(!isUserCustomer)
            findViewById<ViewGroup>(R.id.customerControls).visibility = View.GONE
        else{
            val JSONArray = JSONArray(storage.getString("phone number", arrayOf("''").asList().toString()))
            val phoneNumberContainer = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)
            phoneNumberContainer.removeAllViews()
            for (i in 0 until JSONArray.length()){
                addSimPhoneNumber(phoneNumberContainer,JSONArray.getString(i))
            }
        }
        val fields = if(isUserCustomer) customerFields else delivererFields
        fields.map { e->
            /*if(e.key==R.id.phone_number)
                findViewById<TextInputEditText>(e.key).se*/
            findViewById<EditText>(e.key).setText(
                storage.getString(e.value,"")
            )
        }

    }
    fun requestPermission(){
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_PHONE_STATE)
        val missingPermission = ArrayList<String>()
        for (permission in permissions)
            if(ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED)
                missingPermission.add(permission)
        if(missingPermission.size>0)
            ActivityCompat.requestPermissions(this,missingPermission.toTypedArray(),123)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            if(!Settings.canDrawOverlays(this)){
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                )
                startActivityForResult(intent, 123)
            }
        }
    }
}
