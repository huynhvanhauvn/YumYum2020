package com.sbro.yumyum.Models

import java.util.*

class Restaurant {
    var id: String? = null
    var idUser: String? = null
    var brand: String? = null
    var lat = 0.0
    var lng = 0.0
    var imgUrl: String? = null
    var date: Date? = null

    constructor() {}
    constructor(
        id: String?,
        idUser: String?,
        brand: String?,
        lat: Double,
        lng: Double,
        imgUrl: String?,
        date: Date?
    ) {
        this.id = id
        this.idUser = idUser
        this.brand = brand
        this.lat = lat
        this.lng = lng
        this.imgUrl = imgUrl
        this.date = date
    }

}