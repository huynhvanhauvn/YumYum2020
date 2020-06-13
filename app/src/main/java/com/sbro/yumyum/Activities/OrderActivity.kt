package com.sbro.yumyum.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Adapters.OrderAdapter
import com.sbro.yumyum.Models.Order
import com.sbro.yumyum.Models.Restaurant
import com.sbro.yumyum.Models.User
import com.sbro.yumyum.R
import java.util.*

class OrderActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var adapter: OrderAdapter? = null
    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var orders: ArrayList<Order?>? = null
    private var firestore: FirebaseFirestore? = null
    private val isRestaurant = false
    //private val intent: Intent? = null
    private var mUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView = findViewById<View>(R.id.order_recycler) as RecyclerView
        recyclerView!!.layoutManager = layoutManager

//        intent = getIntent();
//        isRestaurant = intent.getBooleanExtra("isRestaurant", false);
        firestore = FirebaseFirestore.getInstance()
        firestore!!.collection("users").document(user!!.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mUser = task.result
                        ?.toObject(User::class.java)
                    if (mUser!!.isHasRestaurant) {
                        firestore!!.collection("restaurants")
                            .whereEqualTo("idUser", user!!.uid).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var restaurant = Restaurant()
                                    for (snapshot in task.result!!) {
                                        restaurant =
                                            snapshot.toObject(Restaurant::class.java)
                                        restaurant.id = snapshot.id
                                    }
                                    firestore!!.collection("orders")
                                        .whereEqualTo("idRes", restaurant.id).get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                orders =
                                                    ArrayList()
                                                for (snapshot in task.result!!) {
                                                    val order =
                                                        snapshot.toObject(
                                                            Order::class.java
                                                        )
                                                    order.id = snapshot.id
                                                    orders!!.add(order)
                                                }
                                                Collections.sort(
                                                    orders,
                                                    object :
                                                        Comparator<Order?> {
                                                        override fun compare(
                                                            o1: Order?,
                                                            o2: Order?
                                                        ): Int {
                                                            return o2!!.date!!.compareTo(o1!!.date)
                                                        }
                                                    })
                                                adapter = OrderAdapter(
                                                    applicationContext,
                                                    orders
                                                )
                                                recyclerView!!.adapter = adapter
                                                adapter!!.setOnItemClickListener(object :
                                                    OrderAdapter.OnItemClickListener {
                                                    override fun OnItemClick(
                                                        view: View?,
                                                        position: Int
                                                    ) {
                                                        val intent1 = Intent(
                                                            this@OrderActivity,
                                                            OrderDetailActivity::class.java
                                                        )
                                                        intent1.putExtra(
                                                            "id",
                                                            orders!![position]!!.id
                                                        )
                                                        startActivity(intent1)
                                                    }
                                                })
                                            }
                                        }
                                }
                            }
                    } else {
                        firestore!!.collection("orders").whereEqualTo("idUser", user!!.uid)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    orders =
                                        ArrayList()
                                    for (snapshot in task.result!!) {
                                        val order =
                                            snapshot.toObject(
                                                Order::class.java
                                            )
                                        order.id = snapshot.id
                                        orders!!.add(order)
                                    }
                                    adapter = OrderAdapter(applicationContext, orders)
                                    recyclerView!!.adapter = adapter
                                    adapter!!.setOnItemClickListener(object :
                                        OrderAdapter.OnItemClickListener {
                                        override fun OnItemClick(
                                            view: View?,
                                            position: Int
                                        ) {
                                            val intent1 = Intent(
                                                this@OrderActivity,
                                                OrderDetailActivity::class.java
                                            )
                                            intent1.putExtra("id", orders!![position]!!.id)
                                            startActivity(intent1)
                                        }
                                    })
                                }
                            }
                    }
                }
            }
    }
}