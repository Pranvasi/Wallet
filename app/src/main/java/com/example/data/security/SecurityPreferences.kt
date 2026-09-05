package com.example.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "wallet_security_prefs")

data class SecuritySettings(
    val isSetupCompleted: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val autoLockDelaySeconds: Int = 0, // 0 = immediately
    val isSecureScreenEnabled: Boolean = true,
    val themeMode: String = "system", // "system", "light", "dark", "amoled"
    val themeColorPalette: String = "monet", // "monet", "sapphire", "emerald", "amber", "amethyst", "rose"
    val lastBackupTimestamp: Long = 0L
)

class SecurityPreferences(private val context: Context) {

    private val KEY_SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
    private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    private val KEY_AUTO_LOCK_DELAY = intPreferencesKey("auto_lock_delay")
    private val KEY_SECURE_SCREEN = booleanPreferencesKey("secure_screen_enabled")
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_THEME_PALETTE = stringPreferencesKey("theme_palette")
    private val KEY_LAST_BACKUP = longPreferencesKey("last_backup_time")

    val settingsFlow: Flow<SecuritySettings> = context.securityDataStore.data.map { prefs ->
        val isSetup = prefs[KEY_SETUP_COMPLETED] ?: false
        val isLockEnabled = prefs[KEY_APP_LOCK_ENABLED] ?: false
        val isBioEnabled = prefs[KEY_BIOMETRIC_ENABLED] ?: true
        SecuritySettings(
            isSetupCompleted = isSetup,
            isAppLockEnabled = isLockEnabled,
            isBiometricEnabled = isBioEnabled,
            autoLockDelaySeconds = prefs[KEY_AUTO_LOCK_DELAY] ?: 0,
            isSecureScreenEnabled = prefs[KEY_SECURE_SCREEN] ?: true,
            themeMode = prefs[KEY_THEME_MODE] ?: "system",
            themeColorPalette = prefs[KEY_THEME_PALETTE] ?: "monet",
            lastBackupTimestamp = prefs[KEY_LAST_BACKUP] ?: 0L
        )
    }

    suspend fun setSetupCompleted(completed: Boolean = true) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_SETUP_COMPLETED] = completed
        }
    }

    suspend fun resetSecuritySettings() {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = false
            prefs[KEY_BIOMETRIC_ENABLED] = true
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
            prefs[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setAutoLockDelay(seconds: Int) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_AUTO_LOCK_DELAY] = seconds
        }
    }

    suspend fun setSecureScreenEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_SECURE_SCREEN] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setThemePalette(palette: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_THEME_PALETTE] = palette
        }
    }

    suspend fun updateLastBackupTimestamp() {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_LAST_BACKUP] = System.currentTimeMillis()
        }
    }

    private fun hashPasscode(passcode: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(passcode.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun setPasscode(passcode: String) {
        val hash = hashPasscode(passcode)
        context.securityDataStore.edit { prefs ->
            prefs[stringPreferencesKey("passcode_hash")] = hash
        }
    }

    suspend fun verifyPasscode(passcode: String): Boolean {
        val prefs = context.securityDataStore.data.first()
        val storedHash = prefs[stringPreferencesKey("passcode_hash")] ?: return false
        return hashPasscode(passcode) == storedHash
    }

    suspend fun hasPasscode(): Boolean {
        val prefs = context.securityDataStore.data.first()
        return !prefs[stringPreferencesKey("passcode_hash")].isNullOrBlank()
    }
}
