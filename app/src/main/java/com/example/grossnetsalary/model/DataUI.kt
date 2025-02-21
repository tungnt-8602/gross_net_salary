package com.example.grossnetsalary.model

data class ConvertedData(
    val insurances: List<Insurance>,
    val deductions: List<Deduction>,
    val minZoneSalaries: List<MinZoneSalary>,
    val personalIncomeTaxes: List<PersonalIncomeTax>,
    val baseSalaries: List<BaseSalary>
)

data class ResultShow(
    val type: Int = 1,
    val input: Int,
    val bhxh: Double,
    val bhyt: Double,
    val bhtn: Double,
    val tntt: Double,
    val gcbt: Int,
    val gcnpt: Int,
    val ttncn: Double,
    val tnct: Double,
    val res: Double
)