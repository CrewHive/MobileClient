package com.example.myapplication.android.ui.core.api.dto

import com.google.gson.annotations.SerializedName

data class SetCompanyDTO(
    @SerializedName("companyName") val companyName: String,
    @SerializedName("userId") val userId: Long
)

data class UserWithTimeParams2DTO(
    val userId: Long,
    val username: String,
    val email: String,
    val companyName: String?,
    val contractType: String?,
    val workableHoursPerWeek: Int?,
    val overtimeHours: Double?,
    val vacationDaysAccumulated: Double?,
    val vacationDaysTaken: Double?,
    val leaveDaysAccumulated: Double?,
    val leaveDaysTaken: Double?
)


data class UserIdAndUsernameDTO(
    val userId: Long,
    val username: String
)

data class UserIdAndUsernameAndHoursDTO(
    val userId: Long,
    val username: String,
    val workableHoursPerWeek: Int?
)


data class ApiError(
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("instance") val instance: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
    @SerializedName("errors") val errors: Map<String, String>? = null
)
