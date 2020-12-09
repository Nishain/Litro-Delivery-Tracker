package com.ndds.litrocustomerlogger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var customerChangeListener: ListenerRegistration

    private var isUserCustomer:Boolean = true

    private  var googleMap:GoogleMap? = null
    private  var marker:Marker? = null
    var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit  var phoneNumber:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        isUserCustomer = getSharedPreferences("localStorage", MODE_PRIVATE).getBoolean("isUserCustomer",true)
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        val mapFragment = ( supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)
            customerChangeListener = db.document("customer/${phoneNumber}")
                .addSnapshotListener { snapshot, e ->
                    if(!snapshot?.contains("proccessCode")!! || snapshot.get("proccessCode")!! as Long == 0L) {
                        Toast.makeText(this,"Your deliverer declined your delivery!",Toast.LENGTH_LONG).show()
                        setResult(555)
                        finish()
                        return@addSnapshotListener
                    }
                    if(snapshot.get("proccessCode")!! as Long == 2L ){
                        setResult(555)
                        displayDeliveryCompletionMessage(getString(R.string.CustomerSuccessMessage)).show()
                    }

                    val locationHashMap = snapshot?.get("delivererLocation") as HashMap<String,Double>?
                    if(locationHashMap!=null){
                        val location = LatLng(locationHashMap["latitude"]!!,locationHashMap["longitude"]!!)
                        updateMarkerPosition(location)
                    }
                }
    }
    fun swapAvailability(v: View){
        val button = (v as Button)
        var isAvailable = (button.text=="I'm available")
        isAvailable = !isAvailable
        button.text = if(isAvailable) "I'm available" else "I'm not available"
        db.document("customer/$phoneNumber").update("isAvailable",isAvailable)
    }



    override fun onDestroy() {
        Toast.makeText(this,"I'm destroying",Toast.LENGTH_SHORT).show()
        customerChangeListener.remove()
        super.onDestroy()
    }
    override fun onBackPressed() {
        finish()
    }
    override fun onMapReady(gm: GoogleMap?) {
        Toast.makeText(this,"app restarted",Toast.LENGTH_SHORT).show()
        googleMap = gm

    }

    fun updateMarkerPosition(location:LatLng){
        if (marker == null)
            marker = googleMap?.addMarker(MarkerOptions().position(location))
        else
            marker!!.position = location
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position,18f))
    }
    fun displayDeliveryCompletionMessage(message:String): AlertDialog {
        val viewGroup = layoutInflater.inflate(R.layout.delivery_completion_message,null)
        viewGroup.findViewById<TextView>(R.id.completionMessage).setText(message)
        return AlertDialog.Builder(this).setView(viewGroup).create()
    }
}
