package com.ndds.litrocustomerlogger

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var serviceConnection: ServiceConnection
    private var foregroundServiceDisconnected: Boolean = false
    private lateinit var locationChangeListener: ListenerRegistration
    private lateinit var statusChangeListener: ListenerRegistration

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var phoneNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        val mapFragment =
            (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

        //binding the foreground service
        serviceConnection=object :
            ServiceConnection{
            override fun onServiceDisconnected(name: ComponentName?) {
                foregroundServiceDisconnected = true
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                foregroundServiceDisconnected = false
            }

            override fun onBindingDied(name: ComponentName?) {
                foregroundServiceDisconnected = true
            }
        }
        bindService(Intent(this,CallInterceptorService::class.java)
            .putExtra("startListeningCompletionStatus",true)
            .putExtra("phoneNumber",phoneNumber)
            ,serviceConnection,Context.BIND_ABOVE_CLIENT)
        statusChangeListener = db.document("customer/${phoneNumber}").addSnapshotListener{snapshot, error ->
            if (snapshot?.contains("processCode")!!) {
                val processCode = snapshot.get("processCode")!! as Long
                if(processCode==1L)
                    return@addSnapshotListener
                /*val toastMessage = if(processCode==0L) "Your deliverer declined your delivery!"
                    else "That was already completed Delivery"
                Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG
                ).show()*/
                if(foregroundServiceDisconnected)
                    displayDeliveryCompletionMessage(
                        getString(if (processCode==0L) R.string.deliveryCancel
                            else R.string.CustomerSuccessMessage),
                        if (processCode==0L) R.drawable.ic_error_black_24dp else R.drawable.ic_check_circle_black_24dp
                    )
                else{
                    Log.d("debug","called 555 end code")
                    setResult(555)
                    finish()
                }
                return@addSnapshotListener
            }
        }
        locationChangeListener = db.document("delivererLocation/${phoneNumber}")
            .addSnapshotListener { snapshot, e ->
                if(!snapshot?.contains("location")!!)
                    return@addSnapshotListener
                val locationHashMap = snapshot.get("location") as HashMap<String, Double>?
                if (locationHashMap != null) {
                    val location =
                        LatLng(locationHashMap["latitude"]!!, locationHashMap["longitude"]!!)
                    updateMarkerPosition(location)
                }
            }
    }

    fun swapAvailability(v: View) {
        val button = (v as Button)
        var isAvailable = (button.text == "I'm available")
        isAvailable = !isAvailable
        button.text = if (isAvailable) "I'm available" else "I'm not available"
        db.document("customer/$phoneNumber").update("isAvailable", isAvailable)
    }


    override fun onDestroy() {
        locationChangeListener.remove()
        statusChangeListener.remove()
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onMapReady(gm: GoogleMap?) {
        googleMap = gm

    }

    fun updateMarkerPosition(location: LatLng) {
        if (marker == null)
            marker = googleMap?.addMarker(MarkerOptions().position(location))
        else
            marker!!.position = location
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18f))
    }

    fun displayDeliveryCompletionMessage(message: String, imageID: Int){
        val viewGroup = layoutInflater.inflate(R.layout.delivery_completion_message, null)
        viewGroup.findViewById<TextView>(R.id.completionMessage).setText(message)
        viewGroup.findViewById<ImageView>(R.id.statusIcon).setImageResource(imageID)
         val dialog = AlertDialog.Builder(this).setView(viewGroup).
         setOnDismissListener { dialog ->
             dialog.dismiss()
             setResult(555)
             finish()
         }.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}
