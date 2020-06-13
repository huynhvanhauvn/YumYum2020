package com.sbro.yumyum.Adapters

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Models.Dish
import com.sbro.yumyum.R
import java.text.DecimalFormat
import java.util.*


class MenuAdapter(private val context: Context, private val dishes: ArrayList<Dish?>?) :
    RecyclerView.Adapter<MenuAdapter.RecyclerViewHolder>() {
    private var firestore: FirebaseFirestore? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_res_disk, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int
    ) {
        val dish = dishes?.get(position)
        holder.txtNo.setText(position.toString() + 1.toString() + "")
        Glide.with(context).load(dish?.imgUrl).centerCrop().into(holder.img)
        holder.txtName.text = dish?.name
        val format = DecimalFormat("0,000đ")
        holder.txtPrice.text = format.format(dish?.price)
        holder.checkBox.isChecked = dish!!.isAvailable
        if (dish?.id != null) {
            holder.checkBox.isEnabled = true
            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("phuong", "Hau")
                firestore = FirebaseFirestore.getInstance()
                firestore!!.collection("dishes").document(dish.id!!)
                    .update("available", isChecked)
            }
        } else {
            holder.checkBox.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return dishes!!.size
    }

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val txtNo: TextView
        val txtName: TextView
        val txtPrice: TextView
        val img: ImageView
        val checkBox: CheckBox

        init {
            txtNo = itemView.findViewById<View>(R.id.item_res_no) as TextView
            txtName = itemView.findViewById<View>(R.id.item_res_name) as TextView
            txtPrice = itemView.findViewById<View>(R.id.item_res_price) as TextView
            img =
                itemView.findViewById<View>(R.id.item_res_img) as ImageView
            checkBox = itemView.findViewById<View>(R.id.item_res_ckb) as CheckBox
        }
    }

    fun removeItem(position: Int) {
        dishes!!.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

}
