package com.ndds.litrocustomerlogger

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.skyfishjy.library.RippleBackground

class DelievererLocationTransmitter : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit  var customerAvailabilityListener: ListenerRegistration
    private lateinit  var phoneNumber:String
    var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delieverer_location_transmitter)
        findViewById<RippleBackground>(R.id.content).startRippleAnimation()
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        if((getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder(this).setTitle("Your GPS is off").setMessage("Turn on your GPS to share your location live with your customer.Would you enable it now?")
                .setPositiveButton("Sure") { dialog, which ->
                    dialog.dismiss()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<TextView>(R.id.address_hint).setText("sharing location with\n${phoneNumber}\nReach\n ${intent.getStringExtra("address")}")
        engageAvailabilityListener()
    }
    fun engageAvailabilityListener(){
        customerAvailabilityListener = db.document("customer/$phoneNumber").addSnapshotListener{ snapshot, e ->
            if(!snapshot?.contains("isAvailable")!! || !snapshot?.getBoolean("isAvailable")!!)
                findViewById<View>(R.id.availabilityHint).visibility = View.VISIBLE
            else
                findViewById<View>(R.id.availabilityHint).visibility = View.GONE
        }
    }
    fun disengageAvailabilityListener(){
        if(customerAvailabilityListener!=null)
            customerAvailabilityListener.remove()
    }

    override fun onPause() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationCallback = null
        disengageAvailabilityListener()
        super.onPause()
    }
    override fun onBackPressed() {
        finish()
    }
    fun endDelivery(v:View){
        db.document("customer/$phoneNumber").update("proccessCode",2)
        setResult(Activity.RESULT_OK, Intent().putExtra("removingPhoneNumber",phoneNumber))
        val dialog = displayDeliveryCompletionMessage(getString(R.string.DelivererCompletionMessage))
        dialog.setOnDismissListener{dialog: DialogInterface? ->
            finish()
        }
        dialog.show()
    }
    fun cancelDelivery(v:View){
        db.document("customer/$phoneNumber").update("proccessCode",0)
            .addOnSuccessListener { result->
                Toast.makeText(this,"Delivery is successfully cancelled", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK,Intent().putExtra("removingPhoneNumber",phoneNumber))
                finish()
            }
    }
    fun displayDeliveryCompletionMessage(message:String): AlertDialog {
        val viewGroup = layoutInflater.inflate(R.layout.delivery_completion_message,null)
        viewGroup.findViewById<TextView>(R.id.completionMessage).setText(message)
        return AlertDialog.Builder(this).setView(viewGroup).create()
    }
    override fun onResume() {
        if( locationCallback != null)
            return
        engageAvailabilityListener()
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback  = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                disengageAvailabilityListener()
                val lastPosition = LatLng(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
                db.document("customer/${intent.getStringExtra("phoneNumber")}")
                    .update("delivererLocation", lastPosition
                    ).addOnCompleteListener { result-> engageAvailabilityListener()}
            }}
        Log.d("debufInfo","requestingUpdates")
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,this.mainLooper)
        super.onResume()
    }

    override fun onDestroy() {
        disengageAvailabilityListener()
        if(locationCallback!=null)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
