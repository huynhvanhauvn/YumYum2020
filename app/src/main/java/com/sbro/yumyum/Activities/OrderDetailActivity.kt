package com.sbro.yumyum.Activities

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Adapters.OrderDishAdapter
import com.sbro.yumyum.Models.Order
import com.sbro.yumyum.Models.User
import com.sbro.yumyum.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class OrderDetailActivity : AppCompatActivity() {
    private var id = ""
    private var order: Order? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: OrderDishAdapter? = null
    private var txtAdrFrom: TextView? = null
    private var txtAdrTo: TextView? = null
    private var txtDate: TextView? = null
    private var txtShip: TextView? = null
    private var txtQuan: TextView? = null
    private var txtPrice: TextView? = null
    private var txtBtn: TextView? = null
    private var btn: CardView? = null
    private var user: FirebaseUser? = null
    private var mUser: User? = null
    private var firestore: FirebaseFirestore? = null
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        user = FirebaseAuth.getInstance().currentUser
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView = findViewById<View>(R.id.od_recycler) as RecyclerView
        txtAdrFrom = findViewById<View>(R.id.od_txt_address_from) as TextView
        txtAdrTo = findViewById<View>(R.id.od_txt_address_to) as TextView
        txtShip = findViewById<View>(R.id.od_txt_ship) as TextView
        txtQuan = findViewById<View>(R.id.od_txt_quantity_total) as TextView
        txtPrice = findViewById<View>(R.id.od_txt_price_total) as TextView
        txtDate = findViewById<View>(R.id.od_txt_date) as TextView
        btn = findViewById<View>(R.id.od_card_btn) as CardView
        txtBtn = findViewById<View>(R.id.od_txt_btn) as TextView
        recyclerView!!.layoutManager = layoutManager
        val intent = intent
        id = intent.getStringExtra("id")
        firestore = FirebaseFirestore.getInstance()
        firestore!!.collection("orders").document(id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val format = DecimalFormat("0,000 đ")
                    order = task.result
                        ?.toObject(Order::class.java)
                    order!!.id = task.result!!.id
                    adapter = OrderDishAdapter(applicationContext, order!!.selectedDishes)
                    recyclerView!!.adapter = adapter
                    txtAdrFrom!!.text =
                        resources.getString(R.string.all_ship_at) + order!!.storeAddress
                    txtAdrTo!!.text = resources.getString(R.string.od_to) + order!!.cusAddress
                    val dateFormat =
                        SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
                    txtDate!!.text = dateFormat.format(order!!.date)
                    txtShip!!.text = format.format(order!!.deliveryFee)
                    var quan = 0
                    var price = order!!.deliveryFee
                    for (selectedDish in order!!.selectedDishes) {
                        quan += selectedDish.quantity
                        price += selectedDish.dish!!.price * selectedDish.quantity
                    }
                    txtQuan!!.text = quan.toString() + ""
                    txtPrice!!.text = format.format(price.toLong())
                    firestore!!.collection("users").document(user!!.uid).get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                mUser = task.result
                                    ?.toObject(User::class.java)
                                if (mUser!!.isHasRestaurant) {
                                    if (order!!.status == Order.PENDING_CONFIRM) {
                                        txtBtn!!.text = resources.getString(R.string.od_confirm)
                                        btn!!.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
                                        btn!!.setOnClickListener {
                                            firestore!!.collection("orders")
                                                .document(order!!.id!!)
                                                .update(
                                                    "status",
                                                    Order.DELIVERED
                                                )
                                            btn!!.setCardBackgroundColor(
                                                resources.getColor(
                                                    R.color.colorPrimary
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    if (order!!.status == Order.PENDING_CONFIRM) {
                                        txtBtn!!.text = resources.getString(R.string.od_cancel)
                                        btn!!.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
                                        btn!!.setOnClickListener {
                                            firestore!!.collection("orders")
                                                .document(order!!.id!!)
                                                .update(
                                                    "status",
                                                    Order.CANCELED
                                                ).addOnCompleteListener{task->
                                                    if (task.isSuccessful) {
//                                                        val document = task.result
//                                                        if (document != null) {
//                                                            Log.d("debug complete firebase", "DocumentSnapshot data: " + "not null")
//                                                        } else {
//                                                            Log.d("debug complete firebase", "No such document")
//                                                        }
                                                        finish()
                                                    } else {
                                                        Log.d("debug complete firebase", "get failed with ", task.exception)
                                                    }
                                                }
                                            btn!!.setCardBackgroundColor(
                                                resources.getColor(
                                                    R.color.colorPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }
    }
}