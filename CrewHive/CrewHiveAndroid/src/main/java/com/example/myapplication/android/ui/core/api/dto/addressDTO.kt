package com.example.myapplication.android.ui.core.api.dto

data class AddressDTO(
    val street: String,
    val city: String,
    val zipCode: String,
    val province: String,
    val country: String
)

fun AddressDTO.singleLine(): String =
    "${street.trim()}, ${zipCode.trim()} ${city.trim()} (${province.trim()}), ${country.trim()}"
