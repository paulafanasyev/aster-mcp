package com.aster.mobileagent

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores MobileAgent chat configuration; API keys are encrypted with Android Keystore. */
class MobileAgentSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun baseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL).orEmpty()
    fun model(): String = prefs.getString(KEY_MODEL, DEFAULT_MODEL).orEmpty()
    fun apiKey(): String = decrypt(prefs.getString(KEY_API_KEY, null))

    fun save(baseUrl: String, model: String, apiKey: String) {
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl.trim().trimEnd('/'))
            .putString(KEY_MODEL, model.trim())
            .putString(KEY_API_KEY, encrypt(apiKey.trim()))
            .apply()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)
        generator.init(256)
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(cipher.iv.size + encrypted.size)
        cipher.iv.copyInto(combined, 0)
        encrypted.copyInto(combined, cipher.iv.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return runCatching {
            val combined = Base64.decode(value, Base64.NO_WRAP)
            require(combined.size > 12)
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        }.getOrDefault("")
    }

    companion object {
        private const val PREFS = "mobile_agent_settings"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_MODEL = "model"
        private const val KEY_API_KEY = "api_key_encrypted"
        private const val KEY_ALIAS = "mobile_agent_api_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val DEFAULT_BASE_URL = "https://api.moonshot.cn/v1"
        const val DEFAULT_MODEL = "kimi-k2.6"
    }
}
