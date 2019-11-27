package com.hyber.android.hybersdkandroid.core

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.hyber.android.hybersdkandroid.R
import com.hyber.android.hybersdkandroid.add.HyberInternal


open class HyberPublicParams {

    open fun notificationBuilder(
        context: Context,
        notificationTextMess: String
    ): NotificationCompat.Builder {
        val intent = Intent(context, context::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, "hyber.push.hyber")
            .setContentText(notificationTextMess)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.googleg_standard_color_18)
            .setPriority(HyberInternal.notification_priority_old(HyberParameters.push_notification_display_priority))
            .setSound(defaultSoundUri)
            //.setVibrate(longArrayOf(1000))
            .setContentIntent(pendingIntent)
        return notificationBuilder
    }
}

internal object HyberParameters {

    //uuid generates only one time
    var hyber_uuid: String = String()

    //its deviceId which we receive from server with answer for hyber_register_new()
    var deviceId: String = String()

    //is procedure for register new device completed or not
    // (true - devise exist on server. )
    // false - it s new device and we need to complete hyber_register_new()
    var registrationstatus: Boolean = false

    var sdkversion: String = "0.2.30"
    var hyber_osType: String = String()
    var hyber_deviceName: String = String()
    var hyber_deviceType: String = String()
    var hyber_user_Password: String = String()
    var hyber_user_msisdn: String = String()
    var hyber_registration_token: String = String()
    var hyber_user_id: String = String()
    var hyber_registration_createdAt: String = String()
    var firebase_registration_token: String = String()
    var hyber_registration_time: String = String()

    var push_notification_display_priority: Int = 2

    //var branch = "test"
    var branch: String = "master"



    fun fun_hyber_url_device_update(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/device/update"
        } else {
            return "https://test-push.hyber.im/api/2.3/device/update"
        }
    }

    fun fun_hyber_url_registration(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/device/registration"
        } else {
            return "https://test-push.hyber.im/api/2.3/device/registration"
        }
    }

    fun fun_hyber_url_revoke(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/device/revoke"
        } else {
            return "https://test-push.hyber.im/api/2.3/device/revoke"
        }
    }

    fun fun_hyber_url_getdeviceall(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/device/all"
        } else {
            return "https://test-push.hyber.im/api/2.3/device/all"
        }
    }


    fun fun_hyber_url_message_callback(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/message/callback"
        } else {
            return "https://test-push.hyber.im/api/2.3/message/callback"
        }
    }

    fun fun_hyber_url_message_dr(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/message/dr"
        } else {
            return "https://test-push.hyber.im/api/2.3/message/dr"
        }
    }

    fun fun_hyber_url_mess_queue(): String {
        if (branch == "master") {
            return "https://push.hyber.im/api/2.3/message/queue"
        } else {
            return "https://test-push.hyber.im/api/2.3/message/queue"
        }
    }


    //urls for rest api HyberApi class
    var hyber_url_registration: String = fun_hyber_url_registration()
    var hyber_url_revoke: String = fun_hyber_url_revoke()
    var hyber_url_getdeviceall: String = fun_hyber_url_getdeviceall()
    var hyber_url_device_update: String = fun_hyber_url_device_update()
    var hyber_url_message_callback: String = fun_hyber_url_message_callback()
    var hyber_url_message_dr: String = fun_hyber_url_message_dr()
    var hyber_url_mess_queue: String = fun_hyber_url_mess_queue()

    fun hyber_url_message_history(timestamp: String): String {
        if (branch == "master") {
            val hyber_url_mess =
                "https://push.hyber.im/api/2.3/message/history?startDate=" + timestamp
            return hyber_url_mess
        } else {
            val hyber_url_mess2 =
                "https://test-push.hyber.im/api/2.3/message/history?startDate=" + timestamp
            return hyber_url_mess2
        }

    }


}

interface HyberAp {}
enum class HyberApC : HyberAp {
    CODE, BODY
}

internal data class HyberDataApi(val code: Int, val body: String, val time: Int)


public data class HyberFunAnswerRegister(
    val code: Int,
    val result: String,
    val description: String,
    val deviceId: String,
    val token: String,
    val userId: String,
    val userPhone: String,
    val createdAt: String
)

internal data class HyberDataApi2(
    val code: Int,
    val body: HyberFunAnswerRegister,
    val time: Int
)


public data class HyberFunAnswerGeneral(
    val code: Int,
    val result: String,
    val description: String,
    val body: String
)
