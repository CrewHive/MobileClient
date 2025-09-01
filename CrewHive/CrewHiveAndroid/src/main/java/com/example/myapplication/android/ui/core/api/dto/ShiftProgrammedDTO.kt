package com.example.myapplication.android.ui.core.api.dto

import EventUserDTO
import com.google.gson.annotations.SerializedName

data class ShiftProgrammedDTO(
    val shiftProgrammedId: Long?,
    val version: Long?,
    @SerializedName("shiftName") val shiftName: String? = null,
    @SerializedName("title") val title: String? = null, // compat vecchio
    val start: String?,
    val end: String?,
    val date: String?,
    val description: String?,
    val color: String?,
    // resta pure, ma spesso sarà nullo/inutile per i nomi
    val users: List<EventUserDTO>?
)

data class ShiftProgrammedOutputDTO(
    val shifts: List<ShiftProgrammedDTO>,
    val usernames: List<UsernameAndUserIdForShiftProgrammedDTO>
)

data class UsernameAndUserIdForShiftProgrammedDTO(
    val username: List<String>,
    val userId: List<Long>,
    val shiftProgrammedId: Long
)
