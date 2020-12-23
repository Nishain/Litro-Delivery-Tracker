package com.ndds.litrocustomerlogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private var NormalPermissionsGranted: Boolean = false
    private var isUserCustomer: Boolean = false
    val customerFields: HashMap<Int, String> = hashMapOf(
        R.id.username to "username",
        // R.id.phone_number to "phone number",
        R.id.address to "address"
    )
    val delivererFields: HashMap<Int, String> = hashMapOf(
        R.id.username to "username"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        requestPermission()
        initValues()


        findViewById<RadioGroup>(R.id.userType).setOnCheckedChangeListener { group, checkedID ->
            val isCustomer = checkedID == R.id.customer
            findViewById<ViewGroup>(R.id.customerControls).visibility =
                if (isCustomer) View.VISIBLE else View.GONE
        }
        findViewById<Button>(R.id.addSimNumberBtn).setOnClickListener { v ->
            val phoneNumberGroup = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)
            addSimPhoneNumber(phoneNumberGroup, "")
        }
        /*(getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                super.onCallStateChanged(state, incomingNumber)
                println("incomingNumber : $incomingNumber")
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)*/
    }

    fun test(v: View) {
        startActivity(Intent(this, LockScreenPopup::class.java))
    }

    fun test2(v: View) {
        startActivity(Intent(this, DelivererHome::class.java))
    }

    fun navigateToMainScreen() {
        startActivity(
            Intent(
                this,
                if (isUserCustomer) CustomerHome::class.java else DelivererHome::class.java
            )
        )
    }

    fun home(v: View) {
        navigateToMainScreen()
    }

    @SuppressLint("MissingPermission")
    fun saveDetails(v: View) {
        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        var editText: EditText

        isUserCustomer =
            findViewById<RadioGroup>(R.id.userType).checkedRadioButtonId == R.id.customer
        val simNumbers = ArrayList<String>()
        if (isUserCustomer) {
            val viewGroup = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)
            for (child in viewGroup.children) {
                val number = child.findViewById<EditText>(R.id.phone_number).text.toString()
                if (number.isEmpty()) {
                    child.findViewById<TextInputEditText>(R.id.phone_number).error = "Field is empty"
                    showEmptyFieldError()
                    return
                }else
                    child.findViewById<TextInputEditText>(R.id.phone_number).error = ""
                simNumbers.add("'$number'")
            }

            storage.edit().putString("phone number", simNumbers.toTypedArray().contentToString())
                .apply()
        }
        val fields = if (isUserCustomer) customerFields else delivererFields
        for (e in fields.entries) {
            editText = findViewById(e.key)
            if (editText.text.toString().isEmpty()) {
                findViewById<TextInputEditText>(e.key).error= "Field is empty"
                showEmptyFieldError()
                return
            }
            else
                findViewById<TextInputEditText>(e.key).error= ""
            storage.edit().putString(e.value, editText.text.toString()).apply()
        }

        storage.edit().putBoolean("isUserCustomer", isUserCustomer).apply()
        Toast.makeText(this, "saved settings!", Toast.LENGTH_SHORT).show()
        navigateToMainScreen()
        finish()
    }

    fun showEmptyFieldError() {
        AlertDialog.Builder(this)
            .setMessage("Some of the fields are empty.Please fill them before continue!")
            .setTitle("Some of the fields are empty")
            .setIcon(R.drawable.error).show()
    }

    fun addSimPhoneNumber(
        phoneNumberContainer: ViewGroup,
        initialNumber: String,
        isHint: Boolean = false
    ) {
        if (isHint)
            Log.d("Debig", "hint added!")
        val simRow = layoutInflater.inflate(R.layout.sim_phonenumber_row, null) as ViewGroup
        phoneNumberContainer.addView(simRow)
        if (isHint)
            simRow.findViewById<EditText>(R.id.phone_number).setHint(initialNumber)
        else
            simRow.findViewById<EditText>(R.id.phone_number).setText(initialNumber)
        if (phoneNumberContainer.childCount == 1)
            simRow.findViewById<View>(R.id.removeNumber).visibility = View.GONE
        else {
            simRow.findViewById<View>(R.id.removeNumber).setOnClickListener { v ->
                phoneNumberContainer.removeView(v.parent as View)//v.getTag(R.id.POSITION_KEY) as Int
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun initValues() {
        val storage = getSharedPreferences("localStorage", Context.MODE_PRIVATE)
        isUserCustomer = storage.getBoolean("isUserCustomer", true)
        findViewById<RadioGroup>(R.id.userType).check(if (isUserCustomer) R.id.customer else R.id.deliverer)
        if (!isUserCustomer)
            findViewById<ViewGroup>(R.id.customerControls).visibility = View.GONE
        else {
            val jsonArray = JSONArray(storage.getString("phone number", "[]"))
            val phoneNumberContainer = findViewById<ViewGroup>(R.id.simPhoneNumberGroup)

            if (jsonArray.length() == 0) {
                createEmptyPhoneNumberFields(phoneNumberContainer)
            }

            for (i in 0 until jsonArray.length()) {
                addSimPhoneNumber(phoneNumberContainer, jsonArray.getString(i))
            }
        }

        val fields = if (isUserCustomer) customerFields else delivererFields
        fields.map { e ->
            findViewById<EditText>(e.key).setText(
                storage.getString(e.value, "")
            )
        }

    }

    fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )
        val missingPermission = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                missingPermission.add(permission)
        }

        if (missingPermission.size > 0) {
            openRequestPermissionAlert(false,missingPermission.toTypedArray())
        }else {
            NormalPermissionsGranted = true
            openRequestPermissionAlert(true)
        }

    }
    private fun openRequestPermissionAlert(isNormalPermissionGranted:Boolean, missingPermission:Array<String>?=null,isLastStep:Boolean = false){
        val permissionCheck : ViewGroup = layoutInflater.inflate(R.layout.granted_permissions,null) as ViewGroup
        if(isLastStep){
            permissionCheck.findViewById<ImageView>(R.id.permissionTick1).setImageResource(
                if(isNormalPermissionGranted)R.drawable.ic_check_circle_black_24dp else R.drawable.ic_error_black_24dp
            )
            permissionCheck.findViewById<ImageView>(R.id.permissionTick2).setImageResource(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
                    R.drawable.ic_check_circle_black_24dp else R.drawable.ic_error_black_24dp
            )
            AlertDialog.Builder(this).setTitle("Alright!")
                .setMessage("Ok now with that you are good to go")
                .setView(permissionCheck)
                .setPositiveButton("Alright!"){dialog, _ -> dialog.dismiss() }.show()
                return
            }
            val permissionType = if(missingPermission!=null) 1 else 2
            val message = if(permissionType==1)
                "Permission is required normal functionality in this app"
            else
                "Permission is required to show popups when running on background and show them over other apps"
            Log.d("debug","RequestType $permissionType")
            if ((permissionType==1) ||
                (permissionType==2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {

                if(permissionType>1)
                    permissionCheck.findViewById<ImageView>(R.id.permissionTick1).setImageResource(
                        if(isNormalPermissionGranted)R.drawable.ic_check_circle_black_24dp else R.drawable.ic_error_black_24dp
                    )
                AlertDialog.Builder(this).setTitle("Permission Required")
                    .setMessage(message)
                    .setView(permissionCheck)
                    .setPositiveButton("Grant permission") { dialog, _ ->
                        if(permissionType==1)
                            ActivityCompat.requestPermissions(
                            this, missingPermission!!, 123
                        )
                        else{
                            val settingsIntent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivityForResult(settingsIntent, 456)
                        }
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

            }
    }
    private fun createEmptyPhoneNumberFields(phoneNumberContainer: ViewGroup) {
        phoneNumberContainer.removeAllViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            (getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager)
                .activeSubscriptionInfoList.forEach { info ->
                    addSimPhoneNumber(
                        phoneNumberContainer,
                        "Sim ${info.displayName} number",
                        true
                    )
                }
        } else
            addSimPhoneNumber(phoneNumberContainer, "Primary Number", true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode==123){
            NormalPermissionsGranted = true

            permissions.forEachIndexed { index,p ->
                if(grantResults[index]!=PackageManager.PERMISSION_GRANTED)
                    NormalPermissionsGranted = false
                if (p == Manifest.permission.READ_PHONE_STATE && grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    createEmptyPhoneNumberFields(findViewById(R.id.simPhoneNumberGroup))
                }
            }
                openRequestPermissionAlert(NormalPermissionsGranted)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==456){
            openRequestPermissionAlert(NormalPermissionsGranted,null,true)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
