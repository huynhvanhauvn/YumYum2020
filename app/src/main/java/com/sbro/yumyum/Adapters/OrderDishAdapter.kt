package com.sbro.yumyum.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbro.yumyum.Models.SelectedDish
import com.sbro.yumyum.R
import java.text.DecimalFormat
import java.util.*



class OrderDishAdapter(
    private val context: Context,
    val selectedDishes: ArrayList<SelectedDish>
) :
    RecyclerView.Adapter<OrderDishAdapter.RecyclerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_order_detail, parent, false)
        return RecyclerViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int
    ) {
        val quantity = intArrayOf(0)
        val selectedDish = selectedDishes[position]
        Glide.with(context).load(selectedDish.dish!!.imgUrl).centerCrop().into(holder.img)
        holder.txtTitle.text = selectedDish.dish!!.name
        val format = DecimalFormat("0,000đ")
        holder.txtPrice.text = format.format(selectedDish.dish!!.price)
        holder.txtQuantity.text = "X" + selectedDish.quantity
    }

    override fun getItemCount(): Int {
        return selectedDishes.size
    }

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val img: ImageView
        val txtTitle: TextView
        val txtPrice: TextView
        val txtQuantity: TextView

        init {
            img =
                itemView.findViewById<View>(R.id.item_od_img) as ImageView
            txtPrice = itemView.findViewById<View>(R.id.item_od_price) as TextView
            txtTitle = itemView.findViewById<View>(R.id.item_od_title) as TextView
            txtQuantity =
                itemView.findViewById<View>(R.id.item_od_txt_quantity) as TextView
        }
    }

}