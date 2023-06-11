package com.example.grossnetsalary.model

data class PersonalIncomeTax(
    val id: Int,
    val title: String,
    val type: Int,
    val value: List<IncomeTaxRange>
) {
    data class IncomeTaxRange(
        val min: Double?,
        val max: Double?,
        val ratio: Double
    )
}