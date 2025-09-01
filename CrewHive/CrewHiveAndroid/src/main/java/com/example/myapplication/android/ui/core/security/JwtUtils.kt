// com/example/myapplication/android/ui/core/security/JwtUtils.kt
package com.example.myapplication.android.ui.core.security

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

data class JwtClaims(
    val userId: Long?,
    val role: String?,
    val companyId: Long?
)

object JwtUtils {

    /** Estrae solo la parte payload (JSON) dal token, oppure null se non valida */
    fun payloadJson(token: String): JSONObject? {
        val raw = token.removePrefix("Bearer ").trim()
        val parts = raw.split(".")
        if (parts.size < 2) return null
        val payloadB64 = parts[1]
        return try {
            val decoded = base64UrlDecode(payloadB64)
            JSONObject(String(decoded, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }

    /** Restituisce i claim tipizzati, se presenti */
    fun extractClaims(token: String): JwtClaims? {
        val json = payloadJson(token) ?: return null

        val userId = json.optString("sub", null)?.toLongOrNull()
            ?: json.optString("userId", null)?.toLongOrNull() // supporto entrambi

        // ruolo singolo (claim "role") o primo ruolo disponibile in array "roles"/"authorities"
        val role: String? = when {
            json.has("role") && !json.isNull("role") -> json.optString("role", null)
            json.has("roles") && !json.isNull("roles") -> json.optJSONArray("roles")?.firstStringOrNull()
            json.has("authorities") && !json.isNull("authorities") -> json.optJSONArray("authorities")?.firstStringOrNull()
            else -> null
        }

        val companyId: Long? = when {
            json.has("companyId") && !json.isNull("companyId") -> {
                val any = json.get("companyId")
                when (any) {
                    is Number -> any.toLong()
                    is String -> any.toLongOrNull()
                    else -> null
                }
            }
            else -> null
        }

        return JwtClaims(userId = userId, role = role, companyId = companyId)
    }

    /** true se il token contiene un companyId non nullo */
    fun isAssociatedWithCompany(token: String): Boolean =
        extractClaims(token)?.companyId != null

    /** Estrae direttamente lo userId, oppure null se non presente */
    fun getUserId(token: String): Long? =
        extractClaims(token)?.userId

    /** ---- NUOVE FUNZIONI UTILI ---- */

    /** Ritorna il ruolo (se presente). Esempio: "ROLE_MANAGER" */
    fun getRole(token: String): String? =
        extractClaims(token)?.role

    /** Ritorna il companyId (se presente) */
    fun getCompanyId(token: String): Long? =
        extractClaims(token)?.companyId

    /** true se il token ha esattamente il ruolo indicato (case-insensitive) */
    fun hasRole(token: String, expected: String): Boolean =
        extractClaims(token)?.role?.equals(expected, ignoreCase = true) == true
                || arrayRolesContains(payloadJson(token), expected)

    /** true se è ROLE_MANAGER */
    fun isManager(token: String): Boolean =
        hasRole(token, "ROLE_MANAGER")

    /** decodifica Base64URL (no padding) usando android.util.Base64 */
    private fun base64UrlDecode(b64Url: String): ByteArray {
        var s = b64Url.replace('-', '+').replace('_', '/')
        val pad = (4 - s.length % 4) % 4
        s += "=".repeat(pad)
        return Base64.decode(s, Base64.NO_WRAP)
    }

    /** Helper: cerca expected in array "roles" o "authorities" */
    private fun arrayRolesContains(json: JSONObject?, expected: String): Boolean {
        if (json == null) return false
        val target = expected.lowercase()
        val rolesArr: JSONArray? = when {
            json.has("roles") && !json.isNull("roles") -> json.optJSONArray("roles")
            json.has("authorities") && !json.isNull("authorities") -> json.optJSONArray("authorities")
            else -> null
        }
        rolesArr ?: return false
        for (i in 0 until rolesArr.length()) {
            val v = rolesArr.optString(i, null) ?: continue
            if (v.equals(expected, ignoreCase = true)) return true
            // Accetta anche formati tipo {"authority":"ROLE_MANAGER"}
            if (rolesArr.optJSONObject(i)?.optString("authority", null)?.lowercase() == target) return true
        }
        return false
    }

    /** JSONArray → primo string element (o null) */
    private fun JSONArray.firstStringOrNull(): String? {
        for (i in 0 until length()) {
            val v = opt(i)
            when (v) {
                is String -> return v
                is JSONObject -> {
                    // comune in Spring Security: [{"authority":"ROLE_X"}]
                    val auth = v.optString("authority", null)
                    if (!auth.isNullOrBlank()) return auth
                }
            }
        }
        return null
    }

    // JwtUtils.kt – aggiunte utili
    fun getExpiryEpochSeconds(token: String): Long? =
        payloadJson(token)?.optLong("exp", 0L)?.takeIf { it > 0L }

    fun isAboutToExpire(token: String, seconds: Long = 60): Boolean {
        val exp = getExpiryEpochSeconds(token) ?: return false
        val now = System.currentTimeMillis() / 1000
        return exp - now <= seconds
    }

}
