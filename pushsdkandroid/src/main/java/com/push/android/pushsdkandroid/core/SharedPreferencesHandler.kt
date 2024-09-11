package com.push.android.pushsdkandroid.core

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import java.io.File
import javax.crypto.AEADBadTagException

@Suppress("unused")
internal class SharedPreferencesHandler(context: Context) {
    private val preferenceDatabase = "push_k_database"
    private var sharedPref: SharedPreferences = initializeSharedPref(context)


    private fun initializeSharedPref(context: Context): SharedPreferences {
        return try {
            getEncryptedSharedPref(context)
        } catch (e: AEADBadTagException) {
            PushSDKLogger.error(
                "Shared preference initializing failed with exception: " +
                        Log.getStackTraceString(e)
            )
            // Handle exception, likely data corruption or key change (e.g. app was uninstalled and installed again)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.deleteSharedPreferences(preferenceDatabase)
            } else {
                val sharedPrefsFile =
                    File(context.filesDir.parent!! + "/shared_prefs/"
                            + preferenceDatabase + ".xml")
                if (sharedPrefsFile.exists()) {
                    sharedPrefsFile.delete()
                }
            }
            getEncryptedSharedPref(context)
        }
    }

    private fun getEncryptedSharedPref(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            preferenceDatabase,
            getMasterKey(context),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun saveString(KEY_NAME: String, text: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, text)
        editor.apply()
    }

    fun save(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.apply()
    }

    fun save(KEY_NAME: String, status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.apply()
    }

    fun getValueString(KEY_NAME: String): String {
        return sharedPref.getString(KEY_NAME, "") ?: ""
    }

    fun getValueInt(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBool(KEY_NAME: String, defaultValue: Boolean): Boolean {
        return sharedPref.getBoolean(KEY_NAME, defaultValue)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(KEY_NAME)
        editor.apply()
    }
}
