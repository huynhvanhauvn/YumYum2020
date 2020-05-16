package com.sbro.yumyum.Adapters

import android.content.Context
import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbro.yumyum.Adapters.SearchAdapter.RecyclerViewHolder
import com.sbro.yumyum.R
import java.util.*

class SearchAdapter(
    private val context: Context,
    private val address: ArrayList<Address?>
) :
    RecyclerView.Adapter<RecyclerViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_search, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val aAdrress = address[position]?.getAddressLine(position)
        holder.txtAdress.text = aAdrress
        holder.line.setOnClickListener { v ->
            if (onItemClickListener != null) {
                onItemClickListener!!.OnItemClick(v, address[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return address.size
    }

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val txtAdress: TextView
        val line: LinearLayout

        init {
            txtAdress = itemView.findViewById<View>(R.id.item_txt_search) as TextView
                line = itemView.findViewById<View>(R.id.item_search_line) as LinearLayout
        }
    }

    fun setOnItemClickListener(onItemClickListener: SearchAdapter.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun OnItemClick(
            view: View?,
            address: Address?
        )
    }

}