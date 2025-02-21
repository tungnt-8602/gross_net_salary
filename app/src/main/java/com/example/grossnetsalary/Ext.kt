package com.example.grossnetsalary

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.grossnetsalary.model.BaseSalary
import com.example.grossnetsalary.model.ConvertedData
import com.example.grossnetsalary.model.Deduction
import com.example.grossnetsalary.model.Insurance
import com.example.grossnetsalary.model.Item
import com.example.grossnetsalary.model.MinZoneSalary
import com.example.grossnetsalary.model.PersonalIncomeTax
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun convertJsonToObject(context: Context): ConvertedData {
    val gson = Gson()
    val jsonString =
        context.assets.open("salary.json").bufferedReader().use { it.readText() }
    val listTax = object : TypeToken<ArrayList<Item>>() {}.type
    val items: ArrayList<Item> = gson.fromJson(jsonString, listTax)

    var part1 = "["
    var part2 = "["
    var part3 = "["
    var part4 = "["
    var part5 = "["

    for (item in items) {
        when (item.type) {
            1 -> part1 += gson.toJson(item) + ","
            2 -> part2 += gson.toJson(item) + ","
            3 -> part3 += gson.toJson(item) + ","
            4 -> part4 += gson.toJson(item) + ","
            5 -> part5 += gson.toJson(item) + ","
        }
    }
    part1 = customString(part1)
    part2 = customString(part2)
    part3 = customString(part3)
    part4 = customString(part4)
    part5 = customString(part5)

    val insurances: List<Insurance> =
        gson.fromJson(part1, object : TypeToken<List<Insurance>>() {}.type)
    val deductions: List<Deduction> =
        gson.fromJson(part2, object : TypeToken<List<Deduction>>() {}.type)
    val minZoneSalarys: List<MinZoneSalary> =
        gson.fromJson(part3, object : TypeToken<List<MinZoneSalary>>() {}.type)
    val personalIncomeTaxs: List<PersonalIncomeTax> =
        gson.fromJson(part4, object : TypeToken<List<PersonalIncomeTax>>() {}.type)
    val baseSalaries: List<BaseSalary> =
        gson.fromJson(part5, object : TypeToken<List<BaseSalary>>() {}.type)

    return ConvertedData(
        insurances,
        deductions,
        minZoneSalarys,
        personalIncomeTaxs,
        baseSalaries
    )
}

fun validateIntInput(str: String): Int {
    return if (str == "") {
        0
    } else {
        str.toInt()
    }
}

fun customString(input: String): String {
    return input.run {
        if (endsWith(",")) {
            dropLast(1) + "]"
        } else {
            this
        }
    }
}

fun View.showKeyboard() {
    this.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}