package com.ndds.litrocustomerlogger

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

abstract class CustomArrayAdapter(context: Context, resource: Int, data:List<String>) :
    ArrayAdapter<String>(context, resource,data),View.OnClickListener {
    var data:List<String> = mutableListOf<String>()
    private var db:FirebaseFirestore
    init {
        this.data = data
        this.db = FirebaseFirestore.getInstance()
    }
    override fun getCount(): Int {
        if(data.isEmpty())
            onDataEmpty()
        return this.data.size
    }
    fun refreshData(newData : List<String>){
        this.data = newData
        notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if(convertView==null){
            val columns = data[position].split("<.>")
            val newView= LayoutInflater.from(context).inflate(R.layout.row_layout,null) as ViewGroup
            newView.findViewById<TextView>(R.id.phoneNumber).text = columns[0]
            newView.findViewById<TextView>(R.id.address).text = columns[1]

            newView.findViewById<View>(R.id.acceptDelivery).setTag(R.id.PHONENUMBER_KEY,columns[0])
            newView.findViewById<View>(R.id.acceptDelivery).setTag(R.id.ADDRESS_KEY,columns[1])

            newView.findViewById<View>(R.id.dismiss).setTag(R.id.POSITION_KEY,position)
            newView.findViewById<View>(R.id.acceptDelivery).setOnClickListener(this)
            newView.findViewById<View>(R.id.dismiss).setOnClickListener(this)
            return  newView
        }
        return convertView
    }


    abstract fun onNavigateToMap(phoneNumber : String,address:String)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.acceptDelivery->{
                onNavigateToMap(v.getTag(R.id.PHONENUMBER_KEY) as String,v.getTag(R.id.ADDRESS_KEY) as String)
            }
            R.id.dismiss->{
                val removingItem = getItem(v.getTag(R.id.POSITION_KEY) as Int)
                Log.d("removong element at ${v.getTag(R.id.POSITION_KEY)}", removingItem.toString())
                remove(removingItem)
                notifyDataSetChanged()
                onRemoveItem(removingItem!!)
            }
        }

    }
    abstract fun onRemoveItem(removedItem:String)
    abstract fun onDataEmpty()

}