package com.example.myapplication.android.ui.core.api.dto

data class CompanyRegisterRequestDTO (
    val companyName: String,
    val companyType: String,
    val address : AddressDTO?
)