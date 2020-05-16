package com.sbro.yumyum.Models

import java.util.*

class Order {
    var id: String? = null
    var idUser //who order
            : String? = null
    var idRes //who are ordered
            : String? = null
    var date //when order
            : Date? = null
    var storeAddress: String? = null
    var cusAddress: String? = null
    var selectedDishes =
        ArrayList<SelectedDish>()
    var deliveryFee = 0
    var status = 0

    constructor() {}
    constructor(
        id: String?,
        idUser: String?,
        idRes: String?,
        date: Date?,
        storeAddress: String?,
        cusAddress: String?,
        status: Int,
        selectedDishes: ArrayList<SelectedDish>,
        deliveryFee: Int
    ) {
        this.id = id
        this.idUser = idUser
        this.idRes = idRes
        this.date = date
        this.storeAddress = storeAddress
        this.cusAddress = cusAddress
        this.status = status
        this.selectedDishes = selectedDishes
        this.deliveryFee = deliveryFee
    }

    companion object {
        const val PENDING_CONFIRM = 0
        const val DELIVERED = 1
        const val CANCELED = -1
    }
}