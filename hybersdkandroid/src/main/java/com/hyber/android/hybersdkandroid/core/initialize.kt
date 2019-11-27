package com.hyber.android.hybersdkandroid.core

import android.content.Context
import java.util.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.android.gms.tasks.OnSuccessListener
import android.app.Activity
import android.util.Log
import android.widget.Toast
import android.provider.Settings.Global.getString
import com.google.android.gms.tasks.Task
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import android.content.ContentValues.TAG


//function for initialization different parameters
//
internal class Initialization(val context: Context) {
    private val sharedPreference: SharedPreference = SharedPreference(context)
    internal val paramsglobal: HyberParameters = HyberParameters

    fun hyber_init(
        hyber_osType1: String,
        hyber_user_msisdn1: String,
        hyber_user_Password1: String,
        hyber_deviceType1: String,
        hyber_deviceName1: String
    ) {
        val registrationstatus: Boolean =
        sharedPreference.getValueBoolien("registrationstatus", false)
        paramsglobal.registrationstatus = registrationstatus

        //val token = FirebaseInstanceId.getInstance().token

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            // Do whatever you want with your token now
            // i.e. store it on SharedPreferences or DB
            // or directly send it to server
            sharedPreference.save("firebase_registration_token", token.toString())
            paramsglobal.firebase_registration_token = token.toString()
        }

        //var hyber_uuid:String = sharedPreference.getValueString("hyber_uuid").toString()
        if (registrationstatus == false) {
            val hyber_uuid = UUID.randomUUID().toString()
            sharedPreference.save("hyber_uuid", hyber_uuid)
            paramsglobal.hyber_uuid = hyber_uuid

            sharedPreference.save("hyber_user_msisdn", hyber_user_msisdn1)
            paramsglobal.hyber_user_msisdn = hyber_user_msisdn1

            sharedPreference.save("hyber_osType", hyber_osType1)
            paramsglobal.hyber_osType = hyber_osType1

            sharedPreference.save("hyber_user_Password", hyber_user_Password1)
            paramsglobal.hyber_user_Password = hyber_user_Password1

            sharedPreference.save("hyber_deviceType", hyber_deviceType1)
            paramsglobal.hyber_deviceType = hyber_deviceType1

            sharedPreference.save("hyber_deviceName", hyber_deviceName1)
            paramsglobal.hyber_deviceName = hyber_deviceName1

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }

            //sharedPreference.save("firebase_registration_token", token.toString())
            //paramsglobal.firebase_registration_token = token.toString()

        } else {

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }


            val hyber_uuid: String = sharedPreference.getValueString("hyber_uuid")!!.toString()
            paramsglobal.hyber_uuid = hyber_uuid

            val devid: String = sharedPreference.getValueString("deviceId")!!.toString()
            paramsglobal.deviceId = devid


            val hyber_user_msisdn: String =
                sharedPreference.getValueString("hyber_user_msisdn")!!.toString()
            paramsglobal.hyber_user_msisdn = hyber_user_msisdn

            val hyber_user_Password: String =
                sharedPreference.getValueString("hyber_user_Password")!!.toString()
            paramsglobal.hyber_user_Password = hyber_user_Password

            val hyber_deviceType: String =
                sharedPreference.getValueString("hyber_deviceType")!!.toString()
            paramsglobal.hyber_deviceType = hyber_deviceType

            val hyber_deviceName: String =
            sharedPreference.getValueString("hyber_deviceName")!!.toString()
            paramsglobal.hyber_deviceName = hyber_deviceName

            val hyber_osType: String = sharedPreference.getValueString("hyber_osType")!!.toString()
            paramsglobal.hyber_osType = hyber_osType

            val hyber_registration_token: String =
                sharedPreference.getValueString("hyber_registration_token")!!.toString()
            paramsglobal.hyber_registration_token = hyber_registration_token

            val hyber_user_id: String =
                sharedPreference.getValueString("hyber_user_id")!!.toString()
            paramsglobal.hyber_user_id = hyber_user_id

            val hyber_registration_createdAt: String =
                sharedPreference.getValueString("hyber_registration_createdAt")!!.toString()
            paramsglobal.hyber_registration_createdAt = hyber_registration_createdAt

            //sharedPreference.save("firebase_registration_token", token.toString())
            //paramsglobal.firebase_registration_token = token.toString()
        }
    }


    fun hyber_init2() {
        val registrationstatus: Boolean =
        sharedPreference.getValueBoolien("registrationstatus", false)
        paramsglobal.registrationstatus = registrationstatus

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            // Do whatever you want with your token now
            // i.e. store it on SharedPreferences or DB
            // or directly send it to server
            sharedPreference.save("firebase_registration_token", token.toString())
            paramsglobal.firebase_registration_token = token.toString()

        }

        //var hyber_uuid:String = sharedPreference.getValueString("hyber_uuid").toString()
        if (registrationstatus == false) {
            val hyber_uuid = UUID.randomUUID().toString()
            sharedPreference.save("hyber_uuid", hyber_uuid)
            paramsglobal.hyber_uuid = hyber_uuid

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }


        } else {

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }

            val hyber_uuid: String = sharedPreference.getValueString("hyber_uuid")!!.toString()
            paramsglobal.hyber_uuid = hyber_uuid

            val devid: String = sharedPreference.getValueString("deviceId")!!.toString()
            paramsglobal.deviceId = devid


            val hyber_user_msisdn: String =
                sharedPreference.getValueString("hyber_user_msisdn")!!.toString()
            paramsglobal.hyber_user_msisdn = hyber_user_msisdn

            val hyber_user_Password: String =
                sharedPreference.getValueString("hyber_user_Password")!!.toString()
            paramsglobal.hyber_user_Password = hyber_user_Password

            val hyber_deviceType: String =
                sharedPreference.getValueString("hyber_deviceType")!!.toString()
            paramsglobal.hyber_deviceType = hyber_deviceType

            val hyber_deviceName: String =
                sharedPreference.getValueString("hyber_deviceName")!!.toString()
            paramsglobal.hyber_deviceName = hyber_deviceName

            val hyber_osType: String = sharedPreference.getValueString("hyber_osType")!!.toString()
            paramsglobal.hyber_osType = hyber_osType

            val hyber_registration_token: String =
                sharedPreference.getValueString("hyber_registration_token")!!.toString()
            paramsglobal.hyber_registration_token = hyber_registration_token

            val hyber_user_id: String =
                sharedPreference.getValueString("hyber_user_id")!!.toString()
            paramsglobal.hyber_user_id = hyber_user_id

            val hyber_registration_createdAt: String =
                sharedPreference.getValueString("hyber_registration_createdAt")!!.toString()
            paramsglobal.hyber_registration_createdAt = hyber_registration_createdAt

        }
    }


    fun hyber_init3() {
        val registrationstatus: Boolean =
            sharedPreference.getValueBoolien("registrationstatus", false)
        paramsglobal.registrationstatus = registrationstatus


        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            // Do whatever you want with your token now
            // i.e. store it on SharedPreferences or DB
            // or directly send it to server
            println("")
            if (token.toString() != "") {
                sharedPreference.save("firebase_registration_token", token.toString())
                paramsglobal.firebase_registration_token = token.toString()
            } else {
                val firebase_registration_token: String =
                    sharedPreference.getValueString("firebase_registration_token")!!.toString()
                paramsglobal.firebase_registration_token = firebase_registration_token
            }
        }

        //var token = FirebaseInstanceId.getInstance().token.toString()
        //println("token old: $token")

        /*
        if (token=="") {
            token = FirebaseInstanceId.getInstance().getInstanceId().getResult()!!.getToken().toString()
            println("token new: $token")
        }
         */

        //var hyber_uuid:String = sharedPreference.getValueString("hyber_uuid").toString()
        if (registrationstatus == false) {
            val hyber_uuid = UUID.randomUUID().toString()
            sharedPreference.save("hyber_uuid", hyber_uuid)
            paramsglobal.hyber_uuid = hyber_uuid

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }


        } else {

            try {
                val hyber_branch: String = sharedPreference.getValueString("hyber_branch")!!.toString()
                if (hyber_branch != "") {
                    paramsglobal.branch = hyber_branch
                }
            }catch (e:Exception){
                println("hyber_branch failed")
            }

            val hyber_uuid: String = sharedPreference.getValueString("hyber_uuid")!!.toString()
            paramsglobal.hyber_uuid = hyber_uuid

            val devid: String = sharedPreference.getValueString("deviceId")!!.toString()
            paramsglobal.deviceId = devid


            val hyber_user_msisdn: String =
                sharedPreference.getValueString("hyber_user_msisdn")!!.toString()
            paramsglobal.hyber_user_msisdn = hyber_user_msisdn

            val hyber_user_Password: String =
                sharedPreference.getValueString("hyber_user_Password")!!.toString()
            paramsglobal.hyber_user_Password = hyber_user_Password

            val hyber_deviceType: String =
                sharedPreference.getValueString("hyber_deviceType")!!.toString()
            paramsglobal.hyber_deviceType = hyber_deviceType

            val hyber_deviceName: String =
                sharedPreference.getValueString("hyber_deviceName")!!.toString()
            paramsglobal.hyber_deviceName = hyber_deviceName

            val hyber_osType: String = sharedPreference.getValueString("hyber_osType")!!.toString()
            paramsglobal.hyber_osType = hyber_osType

            val hyber_registration_token: String =
                sharedPreference.getValueString("hyber_registration_token")!!.toString()
            paramsglobal.hyber_registration_token = hyber_registration_token

            val hyber_user_id: String =
                sharedPreference.getValueString("hyber_user_id")!!.toString()
            paramsglobal.hyber_user_id = hyber_user_id

            val hyber_registration_createdAt: String =
                sharedPreference.getValueString("hyber_registration_createdAt")!!.toString()
            paramsglobal.hyber_registration_createdAt = hyber_registration_createdAt

        }
    }


    fun clearData() {
        sharedPreference.clearSharedPreference()
    }

    fun firebase_init(context: Context) {

    }

    // sharedPreference.save("ddd","sss")
}