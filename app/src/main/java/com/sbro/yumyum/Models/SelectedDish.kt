package com.sbro.yumyum.Models

class SelectedDish {
    var dish: Dish? = null
    var quantity = 0

    constructor() {}
    constructor(dish: Dish?, quantity: Int) {
        this.dish = dish
        this.quantity = quantity
    }

}