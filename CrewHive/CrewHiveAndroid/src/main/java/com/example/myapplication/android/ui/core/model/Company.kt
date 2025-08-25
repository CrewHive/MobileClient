package com.example.myapplication.android.ui.core.model

data class Company(val id: String, val name: String, val type: CompanyType, val address: String?)
enum class CompanyType { HOSPITAL, RESTAURANT, BAR, OTHER }