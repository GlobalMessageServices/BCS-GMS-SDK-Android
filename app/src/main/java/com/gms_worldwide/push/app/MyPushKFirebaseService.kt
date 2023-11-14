package com.gms_worldwide.push.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.gms_worldwide.push.app.models.Message
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

import com.push.android.pushsdkandroid.PushKFirebaseService
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import java.net.URL


const val channelId = "notification_channel_id"
const val channelName = "notification_name"
const val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"
const val BROADCAST_PUSH_DATA_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"
const val BROADCAST_QUEUE_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"
const val NOTIFICATION_CLICK_INTENT_ACTION = "pushsdk.intent.action.notification"

const val NOTIFICATION_GROUP_ID = "pushsdkgms.notification.group"

/**
 * MyPushKFirebaseService extends PushKFirebaseService from Android SDK.
 */
class MyPushKFirebaseService : PushKFirebaseService(
    summaryNotificationTitleAndText = Pair("title", "text"),
    notificationIconResourceId = android.R.drawable.ic_notification_overlay
) {


    /*override fun setNotificationStyle(
        notificationConstruct: NotificationCompat.Builder,
        data: Map<String, String>,
        notificationStyle: PushSdkNotificationManager.NotificationStyle
    ) {
        super.setNotificationStyle(notificationConstruct, data, notificationStyle)
    }*/


    /**
     * Called when data push is received from the Messaging Hub
     */
    override fun onReceiveDataPush(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onReceiveDataPush(
            false,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )


        //can be used to configure, for example set "isDoNotDisturbModeActive" to false,
        // to send notifications in "Do not disturb mode" anyways
        /*    super.onReceiveDataPush(
               appIsInForeground = appIsInForeground,
               isDoNotDisturbModeActive = false,
              areNotificationsEnabled = areNotificationsEnabled,
               isNotificationChannelMuted = isNotificationChannelMuted,
                remoteMessage = remoteMessage
            )*/
    }

    override fun prepareNotification(data: Map<String, String>): NotificationCompat.Builder? {
        return super.prepareNotification(data)

        /*return pushSdkNotificationManager.constructNotification(
                data,
                PushSdkNotificationManager.NotificationStyle.BIG_PICTURE
            )*/
    }


    /**
     * Callback - when notification is sent
     */
    override fun onNotificationSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onNotificationSent(
            appIsInForeground,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )


    }

    /**
     * Callback - when notification will not be sent
     */
    override fun onNotificationWontBeSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onNotificationWontBeSent(
            appIsInForeground,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )
    }

    /**
     * Called when a message is received from firebase.
     * @param remoteMessage a received message.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //Use to make notification using SDK instruments
        super.onMessageReceived(remoteMessage)


        //Use to make custom notification
        /*if (remoteMessage.data.isNotEmpty() && remoteMessage.data["source"] == "Messaging HUB") {

            sendPushBroadcast(remoteMessage)

            val appIsInForeground = isAppInForeground()
            val isDoNotDisturbModeActive =
                pushSdkNotificationManager.isDoNotDisturbModeActive()
            val areNotificationsEnabled =
                pushSdkNotificationManager.areNotificationsEnabled()
            val isNotificationChannelMuted =
                pushSdkNotificationManager.isNotificationChannelMuted(
                    PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID
                )
            sendCustomNotification(
                remoteMessage,
                appIsInForeground,
                isDoNotDisturbModeActive,
                areNotificationsEnabled,
                isNotificationChannelMuted
            )
        }*/
    }


    /**
     * Send custom notification
     */
    private fun sendCustomNotification(
        remoteMessage: RemoteMessage, appIsInForeground: Boolean, isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean, isNotificationChannelMuted: Boolean
    ) {
        val message = Gson().fromJson(
            remoteMessage.data["message"],
            Message::class.java
        )
        try {

            if (generateCustomNotification(message, this)) {
                onNotificationSent(
                    appIsInForeground,
                    isDoNotDisturbModeActive,
                    areNotificationsEnabled,
                    isNotificationChannelMuted,
                    remoteMessage
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if the application is in foreground.
     * @return Boolean.
     */
    private fun isAppInForeground(): Boolean {

        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    /**
     * Send dataPush broadcast, so it could be caught somewhere else.
     * @param remoteMessage a received message.
     */
    private fun sendPushBroadcast(remoteMessage: RemoteMessage) {

        Intent().apply {
            action = PushSDK.BROADCAST_PUSH_DATA_INTENT_ACTION
            putExtra(PushSDK.BROADCAST_PUSH_DATA_EXTRA_NAME, remoteMessage.data["message"])
            sendBroadcast(this)
        }
    }


    /**
     * Generate custom notification.
     * @param message Message.
     */

    private fun generateCustomNotification(message: Message, context: Context): Boolean {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            intent.apply {
                action = PushSDK.NOTIFICATION_CLICK_INTENT_ACTION
                putExtra(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME, message.toString())
            }

            val pendingActivity =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

            var builder: NotificationCompat.Builder =
                NotificationCompat.Builder(applicationContext, channelId).apply {
                    setSmallIcon(R.drawable.logo)
                    setGroup(NOTIFICATION_GROUP_ID)
                    // setGroupSummary(true)
                    setAutoCancel(true)
                    setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                    setOnlyAlertOnce(false)
                    setContentIntent(pendingActivity)
                    /*context.packageManager.getLaunchIntentForPackage(context.applicationInfo.packageName)?.let {
                        //build an intent for notification (click to open the app)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            it.apply {
                                action = PushSDK.NOTIFICATION_CLICK_INTENT_ACTION
                                putExtra(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME, message.toString())
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        setContentIntent(pendingIntent)
                    }*/

                }

            builder = builder.setContent(getRemoteView(message))

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            notificationManager.notify(0, builder.build())

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }


    /**
     * Get custom RemoteView.
     * @param message Message.
     * @return custom RemoteView.
     */
    @SuppressLint("RemoteViewLayout")
    private fun getRemoteView(message: Message): RemoteViews {
        val remoteView = RemoteViews("com.gms_worldwide.push.app", R.layout.notification)

        remoteView.setTextViewText(R.id.title, message.title)
        remoteView.setTextViewText(R.id.message, message.body)

        if (message.image?.url == null || message.image.url.isEmpty()) {
            remoteView.setImageViewResource(R.id.app_logo, R.drawable.logo)
        } else {
            remoteView.setImageViewBitmap(R.id.app_logo, getBitmapFromURL(message.image!!.url))
        }
        return remoteView

    }


    private fun getBitmapFromURL(strURL: String): Bitmap? {
        strURL.let {
            try {
                URL(strURL).openConnection().run {
                    doInput = true
                    connect()
                    return BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                //not an error
                e.printStackTrace()
                return null
            }
        } ?: return null

    }

}