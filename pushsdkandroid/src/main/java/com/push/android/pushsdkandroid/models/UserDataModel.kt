package com.push.android.pushsdkandroid.models

import java.util.*


data class UserDataModel(
    val deviceOS: String,
    val osVersion: String,
    val deviceModel: String,
    val deviceLanguage: String,
    val deviceLanguageEn: String,
    val isoLanguageCode: String,
    val iso3LanguageCode: String,
    val timeZone: TimeZone,
    val timeZoneShort: String,
    val isoCountry: String,
    val iso3Country: String,
    val countryName: String,
    val countryNameEn: String
){

    override fun toString(): String {
        return "deviceOS=$deviceOS\n" +
                "osVersion=$osVersion\n" +
                "deviceModel=$deviceModel\n" +
                "deviceLanguage=$deviceLanguage\n" +
                "deviceLanguageEn=$deviceLanguageEn\n" +
                "isoLanguageCode=$isoLanguageCode\n" +
                "iso3LanguageCode=$iso3LanguageCode\n" +
                "timeZone=$timeZone\n" +
                "timeZoneShort=$timeZoneShort\n" +
                "isoCountry=$isoCountry\n" +
                "iso3Country=$iso3Country\n" +
                "countryName=$countryName\n" +
                "countryNameEn=$countryNameEn"
    }
}
