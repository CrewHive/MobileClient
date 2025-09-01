package com.example.myapplication.android.ui.core.api.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { TokenManager.jwtToken })
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://domestic-horatia-crewhive-23779c2f.koyeb.app/") // cambia col tuo endpoint
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

//http://100.105.186.65:8080/
