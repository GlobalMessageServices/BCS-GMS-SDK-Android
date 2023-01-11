package com.push.android.pushsdkandroid.utils

import android.os.Build
import android.text.TextUtils
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.res.Configuration
import android.telephony.TelephonyManager
import java.util.*

/**
 * Utils for getting info
 */
internal object Info {

    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return try {
            if (model.startsWith(manufacturer)) {
                capitalize(model).toString()
            } else {
                capitalize(manufacturer) + " " + model
            }
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Get android version
     */
    fun getAndroidVersion(): String {
        return try {
            Build.VERSION.RELEASE
        } catch (e: java.lang.Exception) {
            "unknown"
        }
    }

    /**
     * Get device type (phone or tablet)
     */
    fun getPhoneType(context: Context): String {
        return try {
            val flagIsTab: Boolean =
                context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
            if (flagIsTab) {
                PushSDKLogger.debug(
                    context,
                    "Result: Function: get_phone_type, Class: GetInfo, flagisTab: $flagIsTab, answer: tablet"
                )
                "tablet"
            } else {
                PushSDKLogger.debug(
                    context,
                    "Result: Function: get_phone_type, Class: GetInfo, flagisTab: $flagIsTab, answer: phone"
                )
                "phone"
            }
        } catch (e: java.lang.Exception) {
            "unknown"
        }
    }

    /**
     * Get current OS Type
     * @return current OS type
     */
    fun getOSType(): String {
        return "android"
    }

    /**
     * Get device type (phone or tablet)
     */
    fun getDeviceType(context: Context): String {
        return getPhoneType(context)
    }

    /**
     * Capitalize string (why?!?!)
     */
    private fun capitalize(str: String): String? {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


    /**
     * Get current interface language
     * @return current interface language
     */
    fun getLanguage(): String {
        return Locale.getDefault().displayLanguage
    }

    /**
     * Get current interface language iso code
     * @return current interface language iso code
     */
    fun getLanguageISO(): String {
        return Locale.getDefault().language
    }

    /**
     * Get current interface language iso3 code
     * @return current interface language iso3 code
     */
    fun getLanguageISO3(): String {
        return Locale.getDefault().isO3Language
    }

    /**
     * Get current interface language in English
     * @return current interface language in English
     */
    fun getLanguageInEn(): String {
        val loc = Locale(Locale.getDefault().language)
        val locEn = Locale("en")
        return loc.getDisplayLanguage(locEn)
    }


    /**
     * Get device current time zone
     * @return device current time zone
     */
    fun getDeviceTimeZone(): TimeZone {
        return TimeZone.getDefault()
    }

    /**
     * Get device current short(CET, GMT etc) time zone
     * @return device current short(CET, GMT etc) time zone
     */
    fun getDeviceShortTimeZone(): String {
        val tz = TimeZone.getDefault()
        return TimeZone.getTimeZone(tz.id).getDisplayName(false, TimeZone.SHORT)
    }


    /**
     * Get current country iso code in which device is located.
     * @return device current country iso code by mobile network. If it null, returns country iso code by TimeZone region for API 24 and higher.
     * Else return n/a
     */
    fun getCountryIsoCode(context: Context): String {
        return try {
            val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if(tm != null){
                val isoCode = tm.networkCountryIso.uppercase()
                if (isoCode != null && isoCode.length == 2) {
                    isoCode
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        android.icu.util.TimeZone.getRegion(TimeZone.getDefault().id)
                    } else {
                        "n/a"
                    }
                }
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    android.icu.util.TimeZone.getRegion(TimeZone.getDefault().id)
                } else {
                    "n/a"
                }
            }


        } catch (e: Exception) {
            PushSDKLogger.error("An error was occurred while getting country iso code:\n ${e.stackTraceToString()}")
            "n/a"
        }
    }

    /**
     * Get current country iso3 code
     * @return device current country iso3 code
     */
    fun getCountryIso3Code(context: Context): String {
        return try {
            val countryIsoCode = getCountryIsoCode(context)
            Locale("", countryIsoCode).isO3Country
        } catch (e: Exception) {
            "n/a"
        }
    }

    /**
     * Get current country name
     * @return country name
     */
    fun getCountryName(context: Context): String {
        return try {
            val countryIsoCode = getCountryIsoCode(context)
            Locale("", countryIsoCode).displayCountry
        } catch (e: Exception) {
            "n/a"
        }
    }

    /**
     * Get current country name in English
     * @return current country name in English
     */
    fun getCountryInEn(context: Context): String {
        val loc = Locale("", getCountryIsoCode(context))
        val locEn = Locale("en")
        return loc.getDisplayCountry(locEn)
    }

}