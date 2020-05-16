package com.sbro.yumyum.Activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Models.Rating
import com.sbro.yumyum.Models.Restaurant
import com.sbro.yumyum.R
import java.util.*

class RatingActivity : AppCompatActivity() {
    private var img: ImageView? = null
    private var ratingBar: RatingBar? = null
    private var idRes = ""
    private var firestore: FirebaseFirestore? = null
    private var restaurant: Restaurant? = null
    private var mUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val intent = intent
        idRes = intent.getStringExtra("idRes")
        mUser = FirebaseAuth.getInstance().currentUser
        firestore = FirebaseFirestore.getInstance()
        img = findViewById<View>(R.id.rating_img) as ImageView
        ratingBar = findViewById<View>(R.id.rating_bar) as RatingBar
        if (!intent.equals("")) {
            firestore!!.collection("restaurants").document(idRes).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        restaurant =
                            task.result!!.toObject(Restaurant::class.java)
                        Glide.with(applicationContext).load(restaurant!!.imgUrl).centerCrop()
                            .into(img!!)
                    }
                }
        }
        ratingBar!!.onRatingBarChangeListener =
            OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (idRes != "") {
                    val rating1 = Rating()
                    rating1.idRes = idRes
                    rating1.idUser = mUser!!.uid
                    rating1.date = Calendar.getInstance().time
                    rating1.value = rating
                    firestore!!.collection("ratings").document().set(rating1)
                        .addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.rating_thanks),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }
}