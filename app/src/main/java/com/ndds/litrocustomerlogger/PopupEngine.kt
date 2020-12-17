package com.ndds.litrocustomerlogger

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.TextView

class PopupEngine {
      fun popAlertDialog(context: Context, phoneNumber: String, address: String){
        val dialog = AlertDialog.Builder(context).create()
        val window = dialog.window!!
          window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val viewGroup: ViewGroup = LayoutInflater.from(context).
        inflate(R.layout.activity_customer_info_pop,null) as ViewGroup
        viewGroup.findViewById<TextView>(R.id.customerNumberHint).text = "call from $phoneNumber"
        viewGroup.findViewById<TextView>(R.id.customerAddressHint).text = "from address $address"
        dialog.setView(viewGroup)
        dialog.show()
    }
    fun popDelieveryCompletionAlert(context: Context,message:String) {
        val dialog = AlertDialog.Builder(context).setPositiveButton("Finish")
        {dialog, which ->
            dialog.dismiss()
        }.create()
        val window = dialog.window!!
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val viewGroup: ViewGroup = LayoutInflater.from(context).
        inflate(R.layout.activity_customer_info_pop,null) as ViewGroup
        viewGroup.findViewById<TextView>(R.id.completionMessage).text = message
        dialog.setView(viewGroup)
        dialog.show()
    }
    fun askAdditionalPermissions(context: Context,sharedPreference:SharedPreferences){
        AlertDialog.Builder(context).setTitle("Some necessary permissions are missing")
            .setMessage(context.getString(R.string.additionPermissionRequiredMsg))
            .setPositiveButton("Go to settings") { dialog, which ->
                sharedPreference.edit().remove("lockScreenPopupState").apply()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.getPackageName())
                )
                context.startActivity(intent)
            }.show()
    }
    fun showCredit(context: Context){
        val dialog = AlertDialog.Builder(context).create()
        val creditWindow = LayoutInflater.from(context)
            .inflate(R.layout.credits,null) as ViewGroup
        creditWindow.findViewById<Button>(R.id.endCredit).setOnClickListener{v: View? ->
            dialog.dismiss()
        }
        dialog.setView(creditWindow)
    }
}