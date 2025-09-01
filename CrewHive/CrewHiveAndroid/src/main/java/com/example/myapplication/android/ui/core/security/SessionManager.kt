// com/example/myapplication/android/ui/core/security/SessionManager.kt
package com.example.myapplication.android.ui.core.security

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF = "app_session"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun saveTokens(ctx: Context, access: String?, refresh: String?) {
        prefs(ctx).edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    fun saveAccessToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString(KEY_ACCESS, token).apply()
    }

    fun getToken(ctx: Context): String? =
        prefs(ctx).getString(KEY_ACCESS, null)

    fun getRefreshToken(ctx: Context): String? =
        prefs(ctx).getString(KEY_REFRESH, null)

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}
