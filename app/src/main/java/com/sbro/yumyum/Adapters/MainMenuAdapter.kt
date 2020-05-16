package com.sbro.yumyum.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbro.yumyum.Models.Dish
import com.sbro.yumyum.Models.SelectedDish
import com.sbro.yumyum.R
import java.text.DecimalFormat
import java.util.*

class MainMenuAdapter(
    private val context: Context,
    private val dishes: ArrayList<Dish?>?
) :
    RecyclerView.Adapter<MainMenuAdapter.RecyclerViewHolder>() {
    private val selectedDishes: ArrayList<SelectedDish>
    var total: Int
        private set
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_menu, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int
    ) {
        val quantity = intArrayOf(0)
        val dish = dishes?.get(position)
        selectedDishes.add(SelectedDish(dish, 0))
        if (dish != null) {
            Glide.with(context).load(dish.imgUrl).centerCrop().into(holder.img)
        }
        if (dish != null) {
            holder.txtTitle.text = dish.name
        }
        val format = DecimalFormat("0,000đ")
        if (dish != null) {
            holder.txtPrice.text = format.format(dish.price)
        }
        holder.btnSub.isEnabled = false
        holder.btnAdd.setOnClickListener { v ->
            total++
            quantity[0]++
            holder.txtQuantity.text = quantity[0].toString() + ""
            selectedDishes[position].quantity = quantity[0]
            if (quantity[0] > 0) {
                holder.btnSub.isEnabled = true
            }
            if (onClickListener != null) {
                onClickListener!!.change(v, position, quantity[0])
            }
        }
        holder.btnSub.setOnClickListener { v ->
            total--
            quantity[0]--
            holder.txtQuantity.text = quantity[0].toString() + ""
            selectedDishes[position].quantity = quantity[0]
            if (quantity[0] <= 0) {
                holder.btnSub.isEnabled = false
            }
            if (onClickListener != null) {
                onClickListener!!.change(v, position, quantity[0])
            }
        }
    }

    override fun getItemCount(): Int {
        return dishes!!.size
    }

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val img: ImageView
        val txtTitle: TextView
        val txtPrice: TextView
        val txtQuantity: TextView
        val btnSub: ImageButton
        val btnAdd: ImageButton

        init {
            img =
                itemView.findViewById<View>(R.id.item_menu_img) as ImageView
            txtPrice = itemView.findViewById<View>(R.id.item_menu_price) as TextView
            txtTitle = itemView.findViewById<View>(R.id.item_menu_title) as TextView
            txtQuantity =
                itemView.findViewById<View>(R.id.item_menu_txt_quantity) as TextView
            btnAdd = itemView.findViewById<View>(R.id.item_menu_btn_add) as ImageButton
            btnSub = itemView.findViewById<View>(R.id.item_menu_btn_sub) as ImageButton
        }
    }

    fun getSelectedDishes(): ArrayList<SelectedDish> {
        return selectedDishes
    }

    interface OnClickListener {
        fun change(view: View?, position: Int, quantity: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    init {
        selectedDishes = ArrayList<SelectedDish>()
        total = 0
    }
}