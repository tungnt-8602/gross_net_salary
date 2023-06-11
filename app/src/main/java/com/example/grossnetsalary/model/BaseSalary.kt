package com.example.grossnetsalary.model

data class BaseSalary(val id: Int,
                      val title: String,
                      val type: Int,
                      val value: BaseInfo) {
    data class BaseInfo (val rate: Int,
                         val number: Int)
}
