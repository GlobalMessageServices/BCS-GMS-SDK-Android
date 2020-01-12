package com.hyber.android.hybersdkandroid

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context
import android.app.NotificationManager
import com.hyber.android.hybersdkandroid.core.HyberApi
import com.hyber.android.hybersdkandroid.core.HyberParameters
import com.hyber.android.hybersdkandroid.add.HyberParsing
import java.util.concurrent.Executors
import android.os.Messenger
import android.content.Intent
import com.google.firebase.iid.FirebaseInstanceId
import com.hyber.android.hybersdkandroid.add.RewriteParams
import com.hyber.android.hybersdkandroid.core.HyberPublicParams
import com.hyber.android.hybersdkandroid.core.Initialization
import com.google.firebase.iid.FirebaseInstanceIdService

class FirebaseInstanceId : FirebaseInstanceIdService() {

    private var api: HyberApi = HyberApi()

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        // Get updated InstanceID token.
        val s = FirebaseInstanceId.getInstance().token
        Log.d("FirebaseInstanceid", "Refreshed token: " + s)

        try {
            if (HyberParameters.hyber_registration_token != "" && HyberParameters.firebase_registration_token != "") {
                api.hyber_device_update(
                    HyberParameters.hyber_registration_token,
                    HyberParameters.firebase_registration_token,
                    HyberParameters.hyber_deviceName,
                    HyberParameters.hyber_deviceType,
                    HyberParameters.hyber_osType,
                    HyberParameters.sdkversion,
                    s.toString()
                )
            } else {
                println("Update failed")
            }

        } catch (e: Exception) {
            println("Update failed")
        }


        try {
            if (s.toString() != "") {
                val hyber_update_params: RewriteParams = RewriteParams(applicationContext)
                hyber_update_params.rewrite_firebase_token(s.toString())
            }

        } catch (e: Exception) {
            println("Update2 failed")
        }

        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(refreshedToken)
    }

}

internal class HyberFirebaseService() : FirebaseMessagingService() {

    private val COUNT_PLUS = 1
    private val COUNT_MINUS = 2
    private val SET_COUNT = 0
    private val GET_COUNT = 3

    private var count = 0

    private var messanger: Messenger? = null
    private var toActivityMessenger: Messenger? = null

    private val MSG_KEY = "yo"


    private var api: HyberApi = HyberApi()
    private var parsing: HyberParsing = HyberParsing()


    //private var init_hyber: Initialization = Initialization(applicationContext)
    //private var hyber_main: HyberSDK = HyberSDK(init_hyber.paramsglobal.hyber_user_msisdn, init_hyber.paramsglobal.hyber_user_Password, applicationContext)
    //private var hyber_update_params: RewriteParams = RewriteParams(applicationContext)


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyService onCreate")
        var es = Executors.newFixedThreadPool(1)
        var someRes = Any()

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MyService onDestroy")
        var someRes = null
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            try {

                if (HyberParameters.firebase_registration_token != "" && HyberParameters.hyber_registration_token != "") {

                    api.hyber_message_dr(
                        parsing.parse_message_id(remoteMessage.data.toString()),
                        HyberParameters.firebase_registration_token,
                        HyberParameters.hyber_registration_token
                    )
                    println("delivery report success: messid ${remoteMessage.messageId.toString()}, fbtoken: ${HyberParameters.firebase_registration_token}, hybertoken: ${HyberParameters.hyber_registration_token}")
                } else {
                    println("delivery report failed: messid ${remoteMessage.messageId.toString()}, fbtoken: ${HyberParameters.firebase_registration_token}, hybertoken: ${HyberParameters.hyber_registration_token}")

                }
            } catch (e: Exception) {
                println("failed")
            }

            try {
                HyberPushMess.message = remoteMessage.data.toString()
                val intent = Intent()
                intent.action = "com.hyber.android.hybersdkandroid.Push"
                //intent.putExtra("Data", 1000)
                //intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                sendBroadcast(intent)

            } catch (e: Exception) {
                HyberPushMess.message = ""
            }

            Log.d(TAG, "Message data payload: " + remoteMessage.data.toString())
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)

            try {
                sendNotification(remoteMessage)
            } catch (ee: Exception) {
            }

            /*
            try {
                api.hyber_message_dr(
                    parsing.parse_message_id(remoteMessage.data.toString()),
                    HyberParameters.firebase_registration_token,
                    HyberParameters.hyber_registration_token
                )
                println("delivery report success: messid ${remoteMessage.messageId.toString()}, fbtoken: ${HyberParameters.firebase_registration_token}, hybertoken: ${ HyberParameters.hyber_registration_token}")
            }
            catch (e: Exception) {
                println("failed")
            }

             */

        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    fun sendNotification(remoteMessage: RemoteMessage) {
        val notif: HyberPublicParams = HyberPublicParams()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            0 /* ID of notification */,
            notif.notificationBuilder(
                applicationContext,
                remoteMessage.notification!!.body.toString()
            ).build()
        )
    }

    private companion object {
        private val TAG = "FCMService"
    }
}

