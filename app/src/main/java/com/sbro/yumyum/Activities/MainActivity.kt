package com.sbro.yumyum.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sbro.yumyum.Adapters.MainMenuAdapter
import com.sbro.yumyum.Adapters.SearchAdapter
import com.sbro.yumyum.Activities.CreateResActivity
import com.sbro.yumyum.Models.*
import com.sbro.yumyum.R
import com.sbro.yumyum.Models.MyMarker
import com.sbro.yumyum.Activities.OrderActivity
import java.io.IOException
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mLocation: Location? = null
    private var mMap: GoogleMap? = null
    private var user: FirebaseUser? = null
    private var hasRestaurant = 0
    private var markers: ArrayList<MyMarker>? = null
    private var mapFragment: SupportMapFragment? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var bottomSheet: NestedScrollView? = null
    private var searchView: SearchView? = null

    //private Button btnDirect;
    private var txtResName: TextView? = null
    private var txtQuantity: TextView? = null
    private var txtQuanTotal: TextView? = null
    private var txtAddress: TextView? = null
    private var txtShip: TextView? = null
    private var txtPriceTotal: TextView? = null
    private var recycler: RecyclerView? = null
    private var searchRecycler: RecyclerView? = null
    private var dishes: ArrayList<Dish?>? = null
    private var adapter: MainMenuAdapter? = null
    private var firestore: FirebaseFirestore? = null
    private var markerSearch: Marker? = null
    private var btnOrder: CardView? = null
    private var cart: ConstraintLayout? = null
    private var layoutRating: ConstraintLayout? = null
    private var ratingBar: RatingBar? = null
    private var selectedRestaurant: Restaurant? = null
    private var myRestaurant: Restaurant? = null
    private var tempSelectedDishes: ArrayList<SelectedDish>? = null
    private var shipFee = 0
    private var mPhone: String? = ""
    private var fusedLocationClient: FusedLocationProviderClient? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = intent
        mPhone = intent.getStringExtra("phonenumber")
        user = FirebaseAuth.getInstance().currentUser
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        searchView = findViewById<View>(R.id.main_search) as SearchView
        searchRecycler = findViewById<View>(R.id.main_recycler_search) as RecyclerView
        bottomSheet = findViewById(R.id.main_bottomsheet) as NestedScrollView
        //btnDirect = (Button) bottomSheet.findViewById(R.id.main_btn_direct);
        txtResName =
            bottomSheet!!.findViewById<View>(R.id.main_txt_res_name) as TextView
        txtAddress = bottomSheet!!.findViewById<View>(R.id.main_txt_address) as TextView
        txtShip = bottomSheet!!.findViewById<View>(R.id.main_txt_ship) as TextView
        txtQuanTotal =
            bottomSheet!!.findViewById<View>(R.id.main_txt_quantity_total) as TextView
        txtPriceTotal =
            bottomSheet!!.findViewById<View>(R.id.main_txt_price_total) as TextView
        recycler = bottomSheet!!.findViewById<View>(R.id.main_recycler) as RecyclerView
        btnOrder = bottomSheet!!.findViewById<View>(R.id.main_card_order) as CardView
        cart = findViewById<View>(R.id.main_layout_cart) as ConstraintLayout
        txtQuantity = findViewById<View>(R.id.main_txt_quantity_order) as TextView
        ratingBar = findViewById<View>(R.id.main_rating) as RatingBar
        layoutRating = findViewById<View>(R.id.main_layout_rating) as ConstraintLayout
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler!!.layoutManager = layoutManager
        val layoutManager1 = LinearLayoutManager(applicationContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        searchRecycler!!.layoutManager = layoutManager1
        markers = ArrayList<MyMarker>()
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment_map) as SupportMapFragment?

        //handle search location
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                var addresses =
                    ArrayList<Address?>()
                if (markerSearch != null) {
                    markerSearch!!.remove()
                }
                if (query != null && query != "") {
                    val geocoder = Geocoder(applicationContext)
                    try {
                        addresses = geocoder.getFromLocationName(
                            query,
                            10
                        ) as ArrayList<Address?>
                        if (addresses != null && addresses.size > 0) {
                            //show search list
                            val searchAdapter =
                                SearchAdapter(applicationContext, addresses)
                            searchRecycler!!.adapter = searchAdapter
                            searchRecycler!!.visibility = View.VISIBLE
                            searchAdapter.setOnItemClickListener(object :
                                SearchAdapter.OnItemClickListener {
                                override fun OnItemClick(
                                    view: View?,
                                    address: Address?
                                ) {
                                    val latLng =
                                        LatLng(
                                            address!!.latitude,
                                            address.longitude
                                        )
                                    markerSearch = mMap!!.addMarker(
                                        MarkerOptions().position(latLng).title(query)
                                    )
                                    mMap!!.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            latLng,
                                            16f
                                        )
                                    )
                                    searchRecycler!!.visibility = View.GONE
                                }
                            })
                        } else {
                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.all_search_alert_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        //get all list of restaurants
        getRestaurant()
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("restaurants").whereEqualTo("idUser", user!!.uid)
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (!queryDocumentSnapshots!!.isEmpty) {
                    for (snapshot in queryDocumentSnapshots) {
                        myRestaurant = snapshot.toObject(Restaurant::class.java)
                        myRestaurant!!.id = snapshot.id
                    }
                    if (mPhone != null && mPhone != "") {
                        val mUser = User(
                            user!!.uid,
                            "Lovely User",
                            mPhone,
                            true
                        )
                        firestore.collection("users").document(user!!.uid).set(mUser)
                    }
                    hasRestaurant = 1
                    invalidateOptionsMenu()
                    firestore.collection("orders").whereEqualTo("idRes", myRestaurant!!.id)
                        .whereEqualTo("status", Order.PENDING_CONFIRM)
                        .addSnapshotListener { queryDocumentSnapshots, e ->
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                                txtQuantity!!.text = queryDocumentSnapshots.size().toString() + ""
                                cart!!.visibility = View.VISIBLE
                                cart!!.setOnClickListener {
                                    val intent = Intent(
                                        this@MainActivity,
                                        OrderActivity::class.java
                                    )
                                    intent.putExtra("isRestaurant", true)
                                    startActivity(intent)
                                }
                            } else {
                                txtQuantity!!.text = "0"
                                cart!!.visibility = View.GONE
                            }
                        }
                } else {
                    if (mPhone != null && mPhone != "") {
                        val mUser = User(
                            user!!.uid,
                            "Lovely User",
                            mPhone,
                            false
                        )
                        firestore.collection("users").document(user!!.uid).set(mUser)
                    }
                    hasRestaurant = 2
                    invalidateOptionsMenu()
                    firestore.collection("orders").whereEqualTo("idUser", user!!.uid)
                        .whereEqualTo("status", Order.PENDING_CONFIRM)
                        .addSnapshotListener { queryDocumentSnapshots, e ->
                            if (!queryDocumentSnapshots!!.isEmpty) {
                                txtQuantity!!.text = queryDocumentSnapshots.size().toString() + ""
                                cart!!.visibility = View.VISIBLE
                                cart!!.setOnClickListener {
                                    val intent = Intent(
                                        this@MainActivity,
                                        OrderActivity::class.java
                                    )
                                    intent.putExtra("isRestaurant", false)
                                    startActivity(intent)
                                }
                            } else {
                                txtQuantity!!.text = "0"
                                cart!!.visibility = View.GONE
                            }
                        }
                }
            }
        bottomSheetBehavior = BottomSheetBehavior.from<View>(bottomSheet!!)
        bottomSheetBehavior.setPeekHeight(120)
        bottomSheetBehavior.setFitToContents(true)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(
                bottomSheet: View,
                newState: Int
            ) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchView!!.visibility = View.GONE
                    bottomSheet.background = resources.getDrawable(R.drawable.bg_bottom_sheet_full)
                } else {
                    searchView!!.visibility = View.VISIBLE
                    bottomSheet.background = resources.getDrawable(R.drawable.bg_bottom_sheet)
                }
            }

            override fun onSlide(
                bottomSheet: View,
                slideOffset: Float
            ) {
            }
        })
        btnOrder!!.setOnClickListener {
            if (adapter != null) {
                if (adapter!!.getSelectedDishes() != null && adapter!!.total > 0) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
                    val order = Order()
                    order.idUser = user!!.uid
                    order.idRes = selectedRestaurant!!.id
                    val date = Calendar.getInstance().time
                    order.date = date
                    order.status = Order.PENDING_CONFIRM
                    order.cusAddress =
                        getAddress(mLocation!!.latitude, mLocation!!.longitude)
                    order.storeAddress =
                        getAddress(selectedRestaurant!!.lat, selectedRestaurant!!.lng)
                    order.deliveryFee = shipFee
                    val selectedDishes =
                        ArrayList<SelectedDish>()
                    for (selectedDish in adapter!!.getSelectedDishes()) {
                        if (selectedDish.quantity > 0) {
                            selectedDishes.add(selectedDish)
                        }
                    }
                    order.selectedDishes = selectedDishes
                    val random = Random()
                    val tail = random.nextInt(999999)
                    val id: String = date.time.toString() + tail
                    firestore.collection("orders").document(id).set(order)
                        .addOnSuccessListener { }.addOnFailureListener { }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GPS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mMap!!.isMyLocationEnabled = true
        }
    }


    private fun getRestaurant() {
        val firestore = FirebaseFirestore.getInstance()
        var addOnCompleteListener = firestore.collection("restaurants").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (snapshot in task.result!!) {
                        val restaurant =
                            snapshot.toObject(Restaurant::class.java)
                        restaurant.id = snapshot.id
                        val myMarker =
                            MyMarker(restaurant)
                        markers!!.add(myMarker)
                    }
                    mapFragment!!.getMapAsync(this@MainActivity)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.uiSettings.isMapToolbarEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap!!.isMyLocationEnabled = true
            fusedLocationClient!!.lastLocation
                .addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        mLocation = lastLocation
                        val latLng =
                            LatLng(
                                mLocation!!.latitude,
                                mLocation!!.longitude
                            )
                        mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        if (markers!!.size > 0) {
                            for (myMarker in markers!!) {
                                val location =
                                    Location("res")
                                location.latitude = myMarker.getRestaurant()!!.lat
                                location.longitude = myMarker.getRestaurant()!!.lng
                                val latLngTem =
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                myMarker.marker =
                                    mMap!!.addMarker(
                                        MarkerOptions().position(latLngTem)
                                            .title(myMarker.getRestaurant()!!.brand)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                            .anchor(0.5f, 1f)
                                    )

                                /*if(location.distanceTo(mLocation)<=5000) {
                                                            LatLng latLngTem = new LatLng(location.getLatitude(),
                                                                    location.getLongitude());
                                                            myMarker.setMarker(mMap.addMarker(new MarkerOptions().position(latLngTem)
                                                                    .title(myMarker.getRestaurant().getBrand())
                                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                                                    .anchor(0.5f, 1)));
                                                        }*/
                            }
                        }
                        mMap!!.setOnMarkerClickListener { marker ->
                            for (i in markers!!.indices) {
                                if (marker == markers!![i].marker) {
                                    val format =
                                        DecimalFormat("0,000đ")
                                    selectedRestaurant = markers!![i].getRestaurant()
                                    firestore = FirebaseFirestore.getInstance()
                                    firestore!!.collection("ratings")
                                        .whereEqualTo("idRes", selectedRestaurant!!.id)
                                        .addSnapshotListener { queryDocumentSnapshots, e ->
                                            if (!queryDocumentSnapshots!!.isEmpty) {
                                                var ratingPoint = 0f
                                                var n = 0
                                                for (snapshot in queryDocumentSnapshots) {
                                                    val rating =
                                                        snapshot.toObject(
                                                            Rating::class.java
                                                        )
                                                    ratingPoint += rating.value
                                                    n++
                                                }
                                                ratingBar!!.rating = ratingPoint / n
                                            }
                                        }
                                    ratingBar!!.isEnabled = false
                                    layoutRating!!.setOnClickListener {
                                        firestore!!.collection("ratings")
                                            .whereEqualTo("idUser", user!!.uid).whereEqualTo("idRes", selectedRestaurant!!.id).get()
                                            .addOnCompleteListener { task ->
                                                if (task.result!!.isEmpty) {
                                                    val intent = Intent(
                                                        this@MainActivity,
                                                        RatingActivity::class.java
                                                    )
                                                    intent.putExtra(
                                                        "idRes",
                                                        selectedRestaurant!!.id
                                                    )
                                                    startActivity(intent)
                                                } else {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        resources.getString(R.string.rating_already),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                    txtAddress!!.text =
                                        resources.getString(R.string.all_ship_at) + getAddress(
                                            selectedRestaurant!!.lat,
                                            selectedRestaurant!!.lng
                                        )
                                    val location =
                                        Location("0")
                                    location.latitude = selectedRestaurant!!.lat
                                    location.longitude = selectedRestaurant!!.lng
                                    shipFee =
                                        (mLocation!!.distanceTo(location) * SHIPPING_FEE).toInt()
                                    txtShip!!.text = format.format(shipFee.toLong())
                                    txtResName?.text =
                                        markers!![i].getRestaurant()?.brand

                                    firestore = FirebaseFirestore.getInstance()
                                    firestore!!.collection("dishes")
                                        .whereEqualTo(
                                            "idRes",
                                            markers!![i].getRestaurant()?.id
                                        )
                                        .get().addOnCompleteListener { task ->
                                            dishes = ArrayList()
                                            adapter = MainMenuAdapter(
                                                applicationContext,
                                                dishes
                                            )
                                            recycler!!.adapter = adapter
                                            adapter!!.setOnClickListener(object :
                                                MainMenuAdapter.OnClickListener {
                                                override fun change(
                                                    view: View?,
                                                    position: Int,
                                                    quantity: Int
                                                ) {
                                                    if (tempSelectedDishes != null) {
                                                        tempSelectedDishes!![position].quantity =
                                                            quantity
                                                        var tempTotalQuan = 0
                                                        var tempTotalPrice = 0
                                                        for (selectedDish in tempSelectedDishes!!) {
                                                            tempTotalQuan += selectedDish.quantity
                                                            tempTotalPrice += selectedDish.quantity * selectedDish.dish!!.price
                                                        }
                                                        tempTotalPrice += shipFee
                                                        txtQuanTotal!!.text =
                                                            tempTotalQuan.toString() + ""
                                                        txtPriceTotal!!.text = format.format(
                                                            tempTotalPrice.toLong()
                                                        )
                                                    }
                                                }
                                            })
                                            if (task.isSuccessful && !task.result
                                                    ?.isEmpty()!!
                                            ) {
                                                for (snapshot in task.result!!) {
                                                    val dish =
                                                        snapshot.toObject(
                                                            Dish::class.java
                                                        )
                                                    dish.id = snapshot.id
                                                    if (dish.isAvailable) {
                                                        dishes!!.add(dish)
                                                    }
                                                    tempSelectedDishes =
                                                        ArrayList()
                                                    for (dish1 in dishes!!) {
                                                        val selectedDish =
                                                            SelectedDish(dish1, 0)
                                                        tempSelectedDishes!!.add(
                                                            selectedDish
                                                        )
                                                    }
                                                }
                                                adapter!!.notifyDataSetChanged()
                                                bottomSheetBehavior!!.state =
                                                    BottomSheetBehavior.STATE_HALF_EXPANDED
                                            }
                                        }.addOnFailureListener { }
                                }
                            }
                            false
                        }
                    }
                }
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ), 1
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        if (hasRestaurant == 1) {
            inflater.inflate(R.menu.menu_main_res, menu)
        } else if (hasRestaurant == 2) {
            inflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_add -> {
                val intent =
                    Intent(this@MainActivity, CreateResActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_main_res -> {
                val intent1 = Intent(this@MainActivity, RestaurantActivity::class.java)
                startActivity(intent1)
                true
            }
            R.id.menu_main_logout -> {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                val intent2 = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent2)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    //    @Override
    //    protected void onResume() {
    //        super.onResume();
    //        if(mLocation == null){
    //            getLocation();
    //        }
    //    }
    override fun onBackPressed() {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.app_name))
        builder.setMessage(resources.getString(R.string.main_alert_quit))
        builder.setCancelable(false)
        builder.setPositiveButton(
            resources.getString(R.string.all_yes)
        ) { dialogInterface, i -> finish() }
        builder.setNegativeButton(
            resources.getString(R.string.all_no)
        ) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    companion object {
        private const val SHIPPING_FEE = 3
        private const val GPS_REQUEST_CODE = 100
    }
}