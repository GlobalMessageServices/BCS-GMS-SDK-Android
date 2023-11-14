package com.gms_worldwide.push.app


import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushKFirebaseService
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import com.push.android.pushsdkandroid.settings.BubbleSettings



const val BROADCAST_PUSH_DATA_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"
const val BROADCAST_PUSH_DATA_EXTRA_NAME = "data"


const val NOTIFICATION_GROUP_ID = "pushsdkgms.notification.group"

/**
 * MyPushKFirebaseService extends PushKFirebaseService from Android SDK.
 */
class MyPushKFirebaseService : PushKFirebaseService(
    summaryNotificationTitleAndText = Pair("title", "text"),
    notificationIconResourceId = R.drawable.ic_not_icon,
    bubbleIconResourceId = R.drawable.logo
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

    override fun prepareNotification(
        data: Map<String, String>,
        notificationId: Int
    ): NotificationCompat.Builder? {
        //return super.prepareNotification(data, notificationId)
        val bubbleIntent = Intent(this, BubbleActivity::class.java)
        val bubbleSettings =
            BubbleSettings(setSuppressNotification = true, setAutoExpandBubble = false)
        return pushSdkNotificationManager.constructNotification(
            data,
            notificationId,
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )

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
        println("onMessageReceived ${remoteMessage.notification?.title}")
        println("onMessageReceived ${remoteMessage.notification?.body}")
        println("onMessageReceived ${remoteMessage.notification?.imageUrl}")
        println("onMessageReceived ${remoteMessage.notification?.tag}")
        println("onMessageReceived ${remoteMessage.data}")
        val message = Gson().fromJson(
            remoteMessage.data["message"],
            com.gms_worldwide.push.app.models.PushDataMessageModel::class.java
        )
        println("message received=$message")
        super.onMessageReceived(remoteMessage)

    }


}