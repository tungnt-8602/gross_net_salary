package com.example.grossnetsalary.model

data class MinZoneSalary(
    val id: Int,
    val title: String,
    val type: Int,
    val value: List<ZoneSalary>
) {
    data class ZoneSalary(
        val zone: Int,
        val value: Int,
        val maxbhtn: Int
    )
}