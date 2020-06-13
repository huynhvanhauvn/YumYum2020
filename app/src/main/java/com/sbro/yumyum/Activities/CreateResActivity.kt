package com.sbro.yumyum.Activities

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sbro.yumyum.Activities.RestaurantActivity
import com.sbro.yumyum.Fragments.SelectedLocationFragment
import com.sbro.yumyum.R
import java.io.IOException
import java.util.*

class CreateResActivity : AppCompatActivity() {
    private var txtAddLoca: TextView? = null
    private var lat = 0.0
    private var lng = 0.0
    private var btnIamge: ImageButton? = null
    private var filePath: Uri? = null
    private var btnNext: Button? = null
    private var edtBrand: EditText? = null
    private var imgUrl = ""
    private var myLatlng: DoubleArray? = null
    private var fragment: SelectedLocationFragment? = null
    private var transaction: FragmentTransaction? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_res)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        txtAddLoca = findViewById<View>(R.id.createres_txt_add_loca) as TextView
        btnIamge = findViewById<View>(R.id.createres_btn_add_img) as ImageButton
        btnNext = findViewById<View>(R.id.createres_btn_next) as Button
        edtBrand = findViewById<View>(R.id.createres_edt_brand) as EditText
        txtAddLoca!!.setOnClickListener {
            fragment = SelectedLocationFragment()
            transaction = supportFragmentManager.beginTransaction()
            transaction!!.replace(R.id.createres_fragment_add_loca, fragment!!).commit()
            fragment!!.setOnItemClickListener(object :
                SelectedLocationFragment.OnFragmentInteractionListener {
                override fun onFragmentInteraction(
                    view: View?,
                    latlng: DoubleArray?
                ) {
                    myLatlng = latlng
                    if (myLatlng != null) {
                        lat = myLatlng!![0]
                        lng = myLatlng!![1]
                        txtAddLoca!!.text = getAddress(lat, lng)
                    }
                    val finalLatlng = myLatlng
                    btnNext!!.setOnClickListener {
                        if (edtBrand!!.text.toString() != null && edtBrand!!.text
                                .toString() != ""
                        ) {
                            if (finalLatlng != null && filePath != null) {
                                uploadImage()
                            }
                        } else {
                            edtBrand!!.error = resources.getString(R.string.empty)
                        }
                    }
                    transaction = supportFragmentManager.beginTransaction()
                    transaction!!.remove(fragment!!).commit()
                }
            })
        }
        btnIamge!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }
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
                            val restaurant: MutableMap<String, Any> =
                                HashMap()
                            val auth = FirebaseAuth.getInstance()
                            val user = auth.currentUser
                            restaurant["idUser"] = user!!.uid
                            restaurant["brand"] = edtBrand!!.text.toString()
                            restaurant["lat"] = lat
                            restaurant["lng"] = lng
                            restaurant["imgUrl"] = imgUrl
                            restaurant["date"] = Calendar.getInstance().time
                            val firestore =
                                FirebaseFirestore.getInstance()
                            firestore.collection("restaurants").document().set(restaurant)
                                .addOnSuccessListener {
                                    val intent1 = Intent(
                                        this@CreateResActivity,
                                        RestaurantActivity::class.java
                                    )
                                    intent1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    startActivity(intent1)
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "Fail",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            Toast.makeText(
                                applicationContext,
                                "Uploaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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
                btnIamge!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 101
    }
}