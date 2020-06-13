package com.sbro.yumyum.Models

import com.google.android.gms.maps.model.Marker
import com.sbro.yumyum.Models.Restaurant


class MyMarker {
    private var restaurant: Restaurant? = null
    var marker: Marker? = null

    constructor() {}
    constructor(restaurant: Restaurant?) {
        this.restaurant = restaurant
    }

    fun getRestaurant(): Restaurant? {
        return restaurant
    }

    fun setRestaurant(restaurant: Restaurant?) {
        this.restaurant = restaurant
    }

}