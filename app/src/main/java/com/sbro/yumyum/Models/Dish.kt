package com.sbro.yumyum.Models

class Dish {
    var id: String? = null
    var idRes: String? = null
    var name: String? = null
    var price = 0
    var imgUrl: String? = null
    var isAvailable = false

    constructor() {}
    constructor(
        id: String?,
        idRes: String?,
        name: String?,
        price: Int,
        imgUrl: String?,
        available: Boolean
    ) {
        this.id = id
        this.idRes = idRes
        this.name = name
        this.price = price
        this.imgUrl = imgUrl
        isAvailable = available
    }

}