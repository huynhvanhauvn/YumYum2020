package com.sbro.yumyum.Models

class User {
    var id: String? = null
    var name: String? = null
    var phone: String? = null
    var isHasRestaurant = false

    constructor() {}
    constructor(
        id: String?,
        name: String?,
        phone: String?,
        hasRestaurant: Boolean
    ) {
        this.id = id
        this.name = name
        this.phone = phone
        isHasRestaurant = hasRestaurant
    }

}