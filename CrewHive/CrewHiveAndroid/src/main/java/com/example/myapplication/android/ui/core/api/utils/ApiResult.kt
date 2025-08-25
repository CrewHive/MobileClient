package com.example.myapplication.android.ui.core.api.utils

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data class Unauthorized(val message: String) : ApiResult<Nothing>()
    data class Forbidden(val message: String) : ApiResult<Nothing>()
    data class ServerError(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data class Exception(val message: String) : ApiResult<Nothing>()
    data class  Conflict(val message: String): ApiResult<Nothing>()
}
