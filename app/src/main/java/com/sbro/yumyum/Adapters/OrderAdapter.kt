package com.sbro.yumyum.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Models.Order
import com.sbro.yumyum.Models.Restaurant
import com.sbro.yumyum.R
import java.text.DecimalFormat
import java.util.*

class OrderAdapter(
    private val context: Context,
    private val orders: ArrayList<Order>
) :
    RecyclerView.Adapter<OrderAdapter.RecyclerViewHolder>() {
    private var listener: OnItemClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_order, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int
    ) {
        val order = orders[position]
        holder.txtId.text = order.id
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("restaurants").whereEqualTo("idRes", order.idRes)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (snapshot in task.result!!) {
                        val restaurant =
                            snapshot.toObject(Restaurant::class.java)
                        holder.txtStore.text = restaurant.brand
                    }
                }
            }
        when (order.status) {
            Order.CANCELED -> {
                holder.txtStatus.setText(R.string.all_order_status_cancel)
                holder.txtStatus.setTextColor(context.resources.getColor(R.color.colorCancel))
            }
            Order.DELIVERED -> {
                holder.txtStatus.setText(R.string.all_order_status_success)
                holder.txtStatus.setTextColor(context.resources.getColor(R.color.colorSuccess))
            }
            else -> {
                holder.txtStatus.setText(R.string.all_order_status_pending)
                holder.txtStatus.setTextColor(context.resources.getColor(R.color.colorPending))
            }
        }
        var price = 0
        var quantity = 0
        for (selectedDish in order.selectedDishes) {
            quantity += selectedDish.quantity
            price += selectedDish.quantity * selectedDish.dish!!.price
        }
        if (quantity > 1) {
            holder.txtQuantity.text =
                context.resources.getString(R.string.list_order_have) + " " + quantity + " " + context.resources
                    .getString(R.string.list_order_dishes)
        } else {
            holder.txtQuantity.text =
                context.resources.getString(R.string.list_order_have) + " " + quantity + " " + context.resources
                    .getString(R.string.list_order_dish)
        }
        val format = DecimalFormat("0,000 đ")
        holder.txtPrice.text = format.format(price.toLong())
        holder.line.setOnClickListener {
            if (listener != null) {
                listener!!.OnItemClick(holder.line, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val txtId: TextView
        val txtStore: TextView
        val txtQuantity: TextView
        val txtPrice: TextView
        val txtStatus: TextView
        val line: CardView

        init {
            line = itemView.findViewById<View>(R.id.orderItem_line) as CardView
            txtId = itemView.findViewById<View>(R.id.orderItem_txt_id) as TextView
            txtPrice =
                itemView.findViewById<View>(R.id.orderItem_txt_price) as TextView
            txtQuantity =
                itemView.findViewById<View>(R.id.orderItem_txt_quantity) as TextView
            txtStore =
                itemView.findViewById<View>(R.id.orderItem_txt_store) as TextView
            txtStatus =
                itemView.findViewById<View>(R.id.orderItem_txt_status) as TextView
        }
    }

    interface OnItemClickListener {
        fun OnItemClick(view: View?, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

}