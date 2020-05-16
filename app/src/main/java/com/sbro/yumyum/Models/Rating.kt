package com.sbro.yumyum.Models

import java.util.*

class Rating {
    var idUser: String? = null
    var idRes: String? = null
    var value = 0f
    var date: Date? = null

    constructor() {}
    constructor(
        idUser: String?,
        idRes: String?,
        value: Float,
        date: Date?
    ) {
        this.idUser = idUser
        this.idRes = idRes
        this.value = value
        this.date = date
    }

}