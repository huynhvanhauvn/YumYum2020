package com.sbro.yumyum.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sbro.yumyum.Adapters.MenuAdapter
import com.sbro.yumyum.Models.Dish
import com.sbro.yumyum.Models.Restaurant
import com.sbro.yumyum.R
import com.sbro.yumyum.Utils.SwipeToDeleteCallback
import java.io.IOException
import java.util.*


class RestaurantActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var restaurants: ArrayList<Restaurant?>? = null
    private var firestore: FirebaseFirestore? = null
    private var txtBrand: TextView? = null
    private var txtAdress: TextView? = null
    private var imgAvatar: ImageView? = null
    private var imgDisk: ImageView? = null
    private var header: ConstraintLayout? = null
    private var dishes: ArrayList<Dish?>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: MenuAdapter? = null
    private var edtName: EditText? = null
    private var edtPrice: EditText? = null
    private var btnAdd: ImageButton? = null
    private var filePath: Uri? = null
    private var imgUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        txtBrand = findViewById<View>(R.id.res_txt_brand) as TextView
        txtAdress = findViewById<View>(R.id.res_txt_address) as TextView
        imgAvatar =
            findViewById<View>(R.id.res_img_avatar) as ImageView
        header = findViewById<View>(R.id.res_header) as ConstraintLayout
        recyclerView = findViewById<View>(R.id.res_recycler_menu) as RecyclerView
        edtName = findViewById<View>(R.id.res_edt_name) as EditText
        edtPrice = findViewById<View>(R.id.res_edt_price) as EditText
        btnAdd = findViewById<View>(R.id.res_btn_add) as ImageButton
        imgDisk = findViewById<View>(R.id.res_img_disk) as ImageView
        enableSwipeToDeleteAndUndo()
        btnAdd!!.isEnabled = false
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = layoutManager
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firestore = FirebaseFirestore.getInstance()
        firestore!!.collection("restaurants").whereEqualTo("idUser", user!!.uid)
            .get().addOnCompleteListener { task ->
                restaurants = ArrayList()
                if (task.isSuccessful) {
                    for (snapshot in task.result!!) {
                        val restaurant =
                            snapshot.toObject(Restaurant::class.java)
                        restaurant.id = snapshot.id
                        restaurants!!.add(restaurant)
                    }
                    Glide.with(applicationContext).load(restaurants!![0]!!.imgUrl)
                        .centerCrop().listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                val bitmap = (resource as BitmapDrawable).bitmap
                                Palette.from(bitmap).generate { palette ->
                                    header!!.setBackgroundColor(
                                        palette!!.getDarkVibrantColor(
                                            Color.RED
                                        )
                                    )
                                    txtBrand!!.setTextColor(palette.getMutedColor(Color.WHITE))
                                    txtAdress!!.setTextColor(
                                        palette.getLightMutedColor(
                                            Color.WHITE
                                        )
                                    )
                                }
                                return false
                            }
                        }).into(imgAvatar!!)
                    txtBrand!!.text = restaurants!![0]!!.brand
                    txtAdress!!.text = getAddress(
                        restaurants!![0]!!.lat,
                        restaurants!![0]!!.lng
                    )
                    //get all dishes in menu of the restaurant
                    firestore!!.collection("dishes").whereEqualTo("idRes", restaurants!![0]!!.id)
                        .get()
                        .addOnCompleteListener { task ->
                            dishes = ArrayList()
                            adapter = MenuAdapter(
                                applicationContext,
                                dishes
                            )
                            recyclerView!!.adapter = adapter
                            if (task.isSuccessful && !task.result!!.isEmpty) {
                                for (snapshot in task.result!!) {
                                    val dish =
                                        snapshot.toObject(Dish::class.java)
                                    dish.id = snapshot.id
                                    dishes!!.add(dish)
                                }
                                adapter!!.notifyDataSetChanged()
                                //after getting menu, load into list
                                //                                adapter = new MenuAdapter(getApplicationContext(), dishes);
                                //                                recyclerView.setAdapter(adapter);
                            }
                            btnAdd!!.isEnabled = true
                        }.addOnFailureListener { }
                }
            }.addOnFailureListener { }
        imgDisk!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }
        btnAdd!!.setOnClickListener { //Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
            if ((edtName!!.text != null || edtName!!.text.toString() != "") &&
                (edtPrice!!.text != null || edtPrice!!.text
                    .toString() != "") && filePath != null
            ) {
                uploadImage()
            } else {
                Toast.makeText(applicationContext, R.string.res_error, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                //delete on Database
                firestore!!.collection("dishes").document(dishes!![position]!!.id!!).delete()
                adapter!!.removeItem(position)
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses =
                geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.size > 0) {
                val address = addresses[0]
                result.append(address.getAddressLine(0))
            }
        } catch (e: IOException) {
            Log.e("tag", e.message)
        }
        return result.toString()
    }

    private fun uploadImage() {
        if (filePath != null) {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();
            val storage: FirebaseStorage
            val storageReference: StorageReference
            storage = FirebaseStorage.getInstance()
            storageReference = storage.reference
            val ref =
                storageReference.child("resAvatar/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener { uri ->
                            imgUrl = uri.toString()
                            val disk: MutableMap<String, Any?> =
                                HashMap()
                            disk["idRes"] = restaurants!![0]!!.id
                            disk["name"] = edtName!!.text.toString()
                            Toast.makeText(
                                applicationContext,
                                edtPrice!!.text.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            disk["price"] = edtPrice!!.text.toString().toInt()
                            disk["imgUrl"] = imgUrl
                            disk["available"] = true
                            val firestore =
                                FirebaseFirestore.getInstance()
                            firestore.collection("dishes").document().set(disk)
                                .addOnSuccessListener {
                                    val dish1 = Dish()
                                    dish1.name = edtName!!.text.toString()
                                    dish1.price = edtPrice!!.text.toString().toInt()
                                    dish1.imgUrl = imgUrl
                                    dishes!!.add(dish1)
                                    adapter!!.notifyDataSetChanged()
                                    edtName?.text = null
                                    edtPrice?.text = null
                                    imgDisk!!.setImageResource(R.drawable.photo_icon)
                                }
                        }.addOnFailureListener {
                            Toast.makeText(applicationContext, "Fail", Toast.LENGTH_SHORT)
                                .show()
                        }
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        applicationContext,
                        "Failed " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                            .totalByteCount
                }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            filePath = data.data
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imgDisk!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 101
    }
}

