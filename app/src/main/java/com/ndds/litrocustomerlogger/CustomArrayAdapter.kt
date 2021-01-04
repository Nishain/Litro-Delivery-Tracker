package com.ndds.litrocustomerlogger

import CollapseAnimation
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore

abstract class CustomArrayAdapter(context: Context, resource: Int, data:List<String>) :
    ArrayAdapter<String>(context, resource,data),View.OnClickListener {
    private var deletedPosition: Int = -2
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
        //val position = (data.size-1) - pos
        if(convertView==null || (deletedPosition!=-2 && position>=deletedPosition)){
            if(position==data.size-1)
                deletedPosition = -2
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
                val parent = v.parent.parent.parent as ViewGroup
                val collapseAnimation1 = CollapseAnimation(parent)

                collapseAnimation1.duration = 500
                collapseAnimation1.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        if(collapseAnimation1.phase==1) {
                            collapseAnimation1.phase = 2
                            parent.startAnimation(collapseAnimation1)
                        }else{
                            deletedPosition = v.getTag(R.id.POSITION_KEY) as Int
                               val removingItem = getItem(deletedPosition)
                               remove(removingItem)
                               notifyDataSetChanged()
                               onRemoveItem(removingItem!!)
                        }
                    }

                    override fun onAnimationStart(animation: Animation?) {

                    }
                })
                parent.startAnimation(collapseAnimation1)
            }
        }

    }
    abstract fun onRemoveItem(removedItem:String)
    abstract fun onDataEmpty()

}


