package com.sbro.yumyum.Fragments

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sbro.yumyum.R
import java.io.IOException
import java.util.*

class SelectedLocationFragment : Fragment() {
    private var mMap: GoogleMap? = null
    private var searchView: SearchView? = null
    private var marker: Marker? = null
    private var btnSelect: ImageButton? = null
    private lateinit var latlng: DoubleArray
    private var mapView: MapView? = null
    private var mListener: OnFragmentInteractionListener? =
        null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selected_location, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView =
            view.findViewById<View>(R.id.select_mapView) as MapView
        searchView =
            view.findViewById<View>(R.id.select_search) as SearchView
        btnSelect = view.findViewById<View>(R.id.select_btn) as ImageButton
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                var addresses =
                    ArrayList<Address>()
                if (marker != null) {
                    marker!!.remove()
                }
                if (query != null && query != "") {
                    val geocoder = Geocoder(context)
                    try {
                        addresses = geocoder.getFromLocationName(
                            query,
                            10
                        ) as ArrayList<Address>
                        if (addresses != null && addresses.size > 0) {
                            val address = addresses[0]
                            val latLng =
                                LatLng(
                                    address.latitude,
                                    address.longitude
                                )
                            marker = mMap!!.addMarker(MarkerOptions().position(latLng).title(query))
                            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        } else {
                            Toast.makeText(
                                context,
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
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync { googleMap -> mMap = googleMap }
        btnSelect!!.setOnClickListener { v ->
            if (marker != null) {
                latlng = DoubleArray(2)
                latlng[0] = marker!!.position.latitude
                latlng[1] = marker!!.position.longitude
                onButtonPressed(v, latlng)
            } else {
                Toast.makeText(
                    context!!.applicationContext,
                    R.string.select_empty,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(view: View?, latlng: DoubleArray?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(view, latlng)
        }
    }

    //method to set item click in adapter
    fun setOnItemClickListener(mListener: OnFragmentInteractionListener?) {
        this.mListener = mListener
    }

    //    @Override
    //    public void onAttach(Context context) {
    //        super.onAttach(context);
    //        if (context instanceof OnFragmentInteractionListener) {
    //            mListener = (OnFragmentInteractionListener) context;
    //        } else {
    //            throw new RuntimeException(context.toString()
    //                    + " must implement OnFragmentInteractionListener");
    //        }
    //    }
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(
            view: View?,
            latlng: DoubleArray?
        )
    }

    override fun onResume() {
        mapView!!.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
}