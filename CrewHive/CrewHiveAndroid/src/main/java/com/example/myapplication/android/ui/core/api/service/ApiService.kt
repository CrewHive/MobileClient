package com.example.myapplication.android.ui.core.api.service

import EventDTO
import com.example.myapplication.android.ui.core.api.dto.SetCompanyDTO
import com.example.myapplication.android.ui.core.api.dto.UserIdAndUsernameAndHoursDTO
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO
import com.example.myapplication.android.ui.core.api.dto.CompanyRegisterRequestDTO
import com.example.myapplication.android.ui.core.api.dto.CreateEventDTO
import com.example.myapplication.android.ui.core.api.dto.CreateShiftProgrammedDTO
import com.example.myapplication.android.ui.core.api.dto.CreateShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.dto.LoginRequestDTO
import com.example.myapplication.android.ui.core.api.dto.LoginResponseDTO
import com.example.myapplication.android.ui.core.api.dto.LogoutRequestDTO
import com.example.myapplication.android.ui.core.api.dto.PatchEventDTO
import com.example.myapplication.android.ui.core.api.dto.PatchShiftProgrammedDTO
import com.example.myapplication.android.ui.core.api.dto.PatchShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.dto.RotateRequestDTO
import com.example.myapplication.android.ui.core.api.dto.ShiftProgrammedOutputDTO
import com.example.myapplication.android.ui.core.api.dto.ShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.dto.SignUpRequestDTO
import com.example.myapplication.android.ui.core.api.dto.UpdatePasswordDTO
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParamsDTO
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginResponseDTO>

    @POST("api/auth/register")
    suspend fun signup(@Body request: SignUpRequestDTO): Response<Unit>

    @POST("company/register")
    suspend fun companyRegister(@Body request: CompanyRegisterRequestDTO): Response<LoginResponseDTO>

    @POST("user/logout")
    suspend fun logout(@Body request: LogoutRequestDTO): Response<Unit>

    @GET("user/me")
    suspend fun getCurrentUser(): Response<UserDTO>

    // EVENTI UTENTE
    @GET("event/{temp}/user/{userId}")
    suspend fun getEventsByPeriodAndUser(
        @Path("temp") period: String,   // "DAY" | "WEEK" | "MONTH" | "TRIMESTER" | "SEMESTER" | "YEAR"
        @Path("userId") userId: Long?
    ): List<EventDTO> // vedi DTO sotto

    // EVENTI PUBBLICI (valuta se il backend inferisce la company dal token)
    @GET("event/public/{temp}")
    suspend fun getPublicEventsByPeriod(
        @Path("temp") period: String
    ): List<EventDTO>

    @GET("shift-programmed/period/{period}/user/{userId}")
    suspend fun getShiftsByPeriodAndUser(
        @Path("period") period: String,
        @Path("userId") userId: Long
    ): ShiftProgrammedOutputDTO

    @GET("shift-programmed/period/{period}/company/{companyId}")
    suspend fun getShiftsByPeriodAndCompany(
        @Path("period") period: String,
        @Path("companyId") companyId: Long
    ): ShiftProgrammedOutputDTO


    // ---------- EVENT ----------
    @POST("/event/create")
    suspend fun createEvent(@Body dto: CreateEventDTO): Long

    @PATCH("/event/patch")
    suspend fun patchEvent(@Body dto: PatchEventDTO): Long

    @DELETE("/event/delete/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: Long): Unit

    // ---------- SHIFT PROGRAMMED ----------
    @POST("/shift-programmed/create")
    suspend fun createShift(@Body dto: CreateShiftProgrammedDTO): Long

    @PATCH("/shift-programmed/patch")
    suspend fun patchShift(@Body dto: PatchShiftProgrammedDTO): Long

    @DELETE("/shift-programmed/delete/{shiftId}")
    suspend fun deleteShift(@Path("shiftId") shiftId: Long): Unit

    @PUT("/company/set")
    suspend fun setCompany(@Body body: SetCompanyDTO): Response<Unit>

    @GET("company/{companyId}/users")
    suspend fun getCompanyUsers(
        @Path("companyId") companyId: Long
    ): List<UserIdAndUsernameAndHoursDTO>

    @GET("/user/me")
    suspend fun getMe(): Response<UserWithTimeParams2DTO>

    @POST("api/auth/rotate")
    suspend fun rotate(@Body body: RotateRequestDTO): Response<LoginResponseDTO>

    @PATCH("manager/update-user-work-info")
    suspend fun updateUserWorkInfo(
        @Body dto: com.example.myapplication.android.ui.core.api.dto.UpdateUserWorkInfoDTO
    ): Response<Unit>

    // ---- USER PROFILE ACTIONS ----
    @PATCH("/user/update-username")
    @Headers("Content-Type: text/plain")
    suspend fun updateUsername(@Body body: RequestBody): Response<LoginResponseDTO>

    @PATCH("/user/update-password")
    suspend fun updatePassword(@Body body: UpdatePasswordDTO): Response<Unit>

    @DELETE("/user/leave-company")
    suspend fun leaveCompany(): Response<LoginResponseDTO>

    @DELETE("/user/delete-account")
    suspend fun deleteAccount(): Response<Unit>


    interface AuthApiSync {
        @POST("api/auth/rotate")
        fun rotate(@Body body: RotateRequestDTO): Call<LoginResponseDTO>
    }

    // ---------- SHIFT TEMPLATE ----------
    @POST("/shift-template/create")
    suspend fun createShiftTemplate(@Body dto: CreateShiftTemplateDTO): ShiftTemplateDTO

    @PATCH("/shift-template/update")
    suspend fun updateShiftTemplate(@Body dto: PatchShiftTemplateDTO): ShiftTemplateDTO

    @GET("/shift-template/get/{shiftName}/company/{companyId}")
    suspend fun getShiftTemplate(
        @Path("shiftName") shiftName: String,
        @Path("companyId") companyId: Long
    ): ShiftTemplateDTO

    @DELETE("/shift-template/delete/{shiftName}/company/{companyId}")
    suspend fun deleteShiftTemplate(
        @Path("shiftName") shiftName: String,
        @Path("companyId") companyId: Long
    ): Unit

    // ApiService.kt — aggiungi questa overload SENZA toccare la tua getMe() esistente
    @GET("/user/me")
    suspend fun getMeAs(
        @retrofit2.http.Header("X-Target-User-Id") targetUserId: Long
    ): Response<UserWithTimeParams2DTO>

    @GET("/company/{companyId}/user/{targetId}/info")
    suspend fun getUserInformation(
        @Path("companyId") companyId: Long,
        @Path("targetId") targetId: Long
    ): Response<UserWithTimeParams2DTO>

    // ApiService.kt  — aggiungi in fondo alla sezione USER/COMPANY
    @DELETE("/company/{companyId}/remove/{targetId}")
    suspend fun removeUserFromCompany(
        @Path("companyId") companyId: Long,
        @Path("targetId") targetUserId: Long
    ): Response<Unit>



}

// DTO utente di esempio
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String
)
