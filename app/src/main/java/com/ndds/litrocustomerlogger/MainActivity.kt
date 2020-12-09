package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity()  {
    private var isUserCustomer: Boolean = false
    val valueKeys:HashMap<Int,String> = hashMapOf(
        R.id.username to "username",
        R.id.phone_number to "phone number",
        R.id.address to "address"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initValues()
        requestPermission()
        findViewById<RadioGroup>(R.id.userType).setOnCheckedChangeListener{group,checkedID->
            val shouldEnabled:Boolean = checkedID==R.id.customer
            findViewById<EditText>(R.id.phone_number ).isEnabled = shouldEnabled
            findViewById<EditText>(R.id.address).isEnabled = shouldEnabled
        }
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
    fun saveNumber(v: View){
        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        var editText:EditText

        val isUserCustomer = findViewById<RadioGroup>(R.id.userType).checkedRadioButtonId==R.id.customer
        for (e in valueKeys.entries){
            editText = findViewById(e.key)
            if(isUserCustomer && e.key!=R.id.username  && editText.text.toString().isEmpty()){
                AlertDialog.Builder(this).setMessage("Some of the fields are empty.Please fill them before continue!")
                    .setTitle("Some of the fields are empty")
                    .setIcon(R.drawable.error).show()
                return
            }

            storage.edit().putString(e.value,editText.text.toString()).apply()
        }
        storage.edit().putBoolean("isUserCustomer",isUserCustomer).apply()
        Toast.makeText(this,"saved settings!",Toast.LENGTH_SHORT).show()
        navigateToMainScreen()
        finish()
    }
    fun initValues(){

        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        valueKeys.map { e->
            /*if(e.key==R.id.phone_number)
                findViewById<TextInputEditText>(e.key).se*/
            findViewById<EditText>(e.key).setText(
                storage.getString(e.value,"")
            )
        }
        isUserCustomer = storage.getBoolean("isUserCustomer",true)
        findViewById<RadioGroup>(R.id.userType).check(if(isUserCustomer)R.id.customer else R.id.deliverer)
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
