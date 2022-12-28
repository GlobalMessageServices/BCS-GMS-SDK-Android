/*
Service for Firebase Push notification messaging
 */

package com.push.android.pushsdkandroid

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.core.PushSdkSavedDataProvider
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import com.push.android.pushsdkandroid.models.PushDataMessageModel
import com.push.android.pushsdkandroid.settings.BubbleSettings
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A "FirebaseMessagingService based" service for handling push messages;
 * Extend it and override available callbacks at will;
 * Also don't forget to add your service to AndroidManifest.xml
 *
 * @constructor A "FirebaseMessagingService based" service for handling push messages
 *
 * @param summaryNotificationTitleAndText Summary notification title and text <title, text>,
 * used for displaying a "summary notification", which serves as a root notification for other notifications
 * notifications will not be bundled(grouped) if null; Ignored if api level is below android 7
 *
 * @param notificationIconResourceId Notification small icon
 *
 */
open class PushKFirebaseService(
    private val summaryNotificationTitleAndText: Pair<String, String>?,
    private val notificationIconResourceId: Int = android.R.drawable.ic_notification_overlay,
    private val bubbleIconResourceId: Int = android.R.drawable.ic_dialog_email
) : FirebaseMessagingService() {

    private lateinit var pushSdkSavedDataProvider: PushSdkSavedDataProvider
    private lateinit var apiHandler: APIHandler
    lateinit var pushSdkNotificationManager: PushSdkNotificationManager
    var bubbleIntent: Intent? = null
    var bubbleSettings: BubbleSettings = BubbleSettings()

    /**
     * Called when the service is created
     */
    override fun onCreate() {
        super.onCreate()
        pushSdkSavedDataProvider = PushSdkSavedDataProvider(applicationContext)
        apiHandler = APIHandler(applicationContext)
        pushSdkNotificationManager = PushSdkNotificationManager(
            this,
            summaryNotificationTitleAndText,
            notificationIconResourceId,
            bubbleIconResourceId,
            bubbleIntent,
            bubbleSettings
        )
        PushSDKLogger.debug(applicationContext, "${javaClass.simpleName}.onCreate: service created")

        //onNewToken(pushSdkSavedDataProvider.firebase_registration_token) //debug only
    }

    /**
     * Called when the service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        PushSDKLogger.debug(
            applicationContext,
            "${javaClass.simpleName}.onDestroy: service destroyed"
        )
    }

    /**
     * Called when a new firebase token is obtained
     * @param newToken the new firebase token
     */
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        PushSDKLogger.debug(
            applicationContext,
            "${javaClass.simpleName}.onNewToken: New token received: $newToken"
        )
        if (newToken != "") {
            try {
                val pushSdkSavedDataProvider = PushSdkSavedDataProvider(applicationContext)
                pushSdkSavedDataProvider.firebaseRegistrationToken = newToken
                PushSDKLogger.debug(
                    applicationContext,
                    "${javaClass.simpleName}.onNewToken: local update: success"
                )
            } catch (e: Exception) {
                PushSDKLogger.error(
                    "${javaClass.simpleName}.onNewToken: local update error: ${
                        Log.getStackTraceString(
                            e
                        )
                    }}"
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                updateDeviceRegistration(newToken)
            }
        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }

    /**
     * Called when a message is received from firebase
     * @param remoteMessage the received message
     *
     * There are two types of messages data messages and notification messages. Data messages are handled
     * here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
     * traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
     * is in the foreground. When the app is in the background an automatically generated notification is displayed.
     * When the user taps on the notification they are returned to the app. Messages containing both notification
     * and data payloads are treated as notification messages. The Firebase console always sends notification
     * messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        PushSDKLogger.debug(
            applicationContext,
            "${javaClass.simpleName}.onMessageReceived: started"
        )
        super.onMessageReceived(remoteMessage)

        PushSDKLogger.debugFirebaseRemoteMessage(applicationContext, remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty() && remoteMessage.data["source"] == "Messaging HUB") {

            //data push received, make a callback
            val isDoNotDisturbModeActive = pushSdkNotificationManager.isDoNotDisturbModeActive()
            val areNotificationsEnabled = pushSdkNotificationManager.areNotificationsEnabled()
            val isNotificationChannelMuted =
                pushSdkNotificationManager.isNotificationChannelMuted(PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID)
            onReceiveDataPush(
                isAppInForeground(),
                isDoNotDisturbModeActive,
                areNotificationsEnabled,
                isNotificationChannelMuted,
                remoteMessage
            )
        }
    }

    /**
     * Called when firebase deletes some messages (they will never be delivered);
     * When the app instance receives this callback, it should perform a full sync with your app server
     *
     * In some situations, FCM may not deliver a message.
     * This occurs when there are too many messages (>100) pending for your app on a particular
     * device at the time it connects or if the device hasn't connected to FCM in more than one month.
     * In these cases, you may receive a callback to FirebaseMessagingService.onDeletedMessages()
     * When the app instance receives this callback, it should perform a full sync with your app server.
     * If you haven't sent a message to the app on that device within the last 4 weeks, FCM won't call onDeletedMessages()
     */

    @Suppress("RedundantOverride")
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        //TODO sync something in here
    }

    /**
     * Called when the service receives a FCM push containing data; Override it
     * without the "super" call, if you want to implement your own notifications or disable them;
     * Delivery reports for "data pushes" have to be sent manually
     *
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param isDoNotDisturbModeActive whether user has "Do not disturb" mode on
     * @param areNotificationsEnabled whether notifications are enabled for the app
     * @param isNotificationChannelMuted whether the notification channel is muted on the user device
     * @param remoteMessage received remote message object
     */
    open fun onReceiveDataPush(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        PushSDKLogger.debug(
            applicationContext,
            "onReceiveDataPush(): \n" +
                    "appIsInForeground: $appIsInForeground \n" +
                    "isDoNotDisturbModeActive: $isDoNotDisturbModeActive \n" +
                    "areNotificationsEnabled: $areNotificationsEnabled \n" +
                    "isNotificationChannelMuted: $isNotificationChannelMuted \n" +
                    "remoteMessage.data: ${remoteMessage.data}"
        )
        //send notification
        if (remoteMessage.notification == null
            && !appIsInForeground
            && !isDoNotDisturbModeActive
            && areNotificationsEnabled
            && !isNotificationChannelMuted
            && pushSdkNotificationManager.hasSpaceForNotification(true)
        ) {
            val notificationConstruct = prepareNotification(remoteMessage.data)
            if (notificationConstruct != null) {
                var isNotificationSent =
                    pushSdkNotificationManager.sendNotification(notificationConstruct)
                if (isNotificationSent) {
                    onNotificationSent(
                        appIsInForeground,
                        isDoNotDisturbModeActive,
                        areNotificationsEnabled,
                        isNotificationChannelMuted,
                        remoteMessage
                    )
                } else {
                    PushSDKLogger.error(
                        "Unable to create send notification: \n" +
                                "notificationConstruct: $notificationConstruct"
                    )
                    onNotificationWontBeSent(
                        appIsInForeground,
                        isDoNotDisturbModeActive,
                        areNotificationsEnabled,
                        isNotificationChannelMuted,
                        remoteMessage
                    )
                }
            } else {
                PushSDKLogger.error(
                    "Unable to create notificationConstruct: \n" +
                            "remoteMessage.data: ${remoteMessage.data}"
                )
                onNotificationWontBeSent(
                    appIsInForeground,
                    isDoNotDisturbModeActive,
                    areNotificationsEnabled,
                    isNotificationChannelMuted,
                    remoteMessage
                )
            }
        } else {
            PushSDKLogger.debug(
                applicationContext,
                "Can't send notification: \n" +
                        "appIsInForeground: $appIsInForeground \n" +
                        "isDoNotDisturbModeActive: $isDoNotDisturbModeActive \n" +
                        "areNotificationsEnabled: $areNotificationsEnabled \n" +
                        "isNotificationChannelMuted: $isNotificationChannelMuted \n" +
                        "remoteMessage.data: ${remoteMessage.data}"
            )
            onNotificationWontBeSent(
                appIsInForeground,
                isDoNotDisturbModeActive,
                areNotificationsEnabled,
                isNotificationChannelMuted,
                remoteMessage
            )
        }
    }

    /**
     * Prepares NotificationCompat.Builder object for sending;
     * @param data - FCM push RemoteMessage's data
     * @return NotificationCompat.Builder?
     * @see PushSdkNotificationManager.NotificationStyle, (https://developer.android.com/training/notify-user/group), (https://stackoverflow.com/a/41114135)
     */
    open fun prepareNotification(data: Map<String, String>): NotificationCompat.Builder? {
        PushSDKLogger.debug(applicationContext, "calling prepareNotification()")
        return pushSdkNotificationManager.constructNotification(
            data,
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )
    }

    /**
     * Called when notification is sent; Will send message delivery report here;
     * Displaying a notification is not guaranteed;
     * The method will not be called if onReceiveDataPush wasn't called before
     *
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param isDoNotDisturbModeActive whether user has "Do not disturb" mode on
     * @param areNotificationsEnabled whether notifications are enabled for the app
     * @param isNotificationChannelMuted whether the notification channel is muted on the user device
     * @param remoteMessage received remote message object
     */
    open fun onNotificationSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        PushSDKLogger.debug(applicationContext, "calling onNotificationSent()")
        if (pushSdkSavedDataProvider.enableAutoDeliveryReport) {
            sendMessageDeliveryReport(remoteMessage)
        }
    }

    /**
     * Called when notification will not be sent, so you can try sending it manually;
     * The method will not be called if onReceiveDataPush wasn't called before
     *
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param isDoNotDisturbModeActive whether user has "Do not disturb" mode on
     * @param areNotificationsEnabled whether notifications are enabled for the app
     * @param isNotificationChannelMuted whether the notification channel is muted on the user device
     * @param remoteMessage received remote message object
     */
    open fun onNotificationWontBeSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        PushSDKLogger.debug(applicationContext, "calling onNotificationWontBeSent()")
        //does nothing
    }

    /**
     * Check if the app is currently in foreground or background
     * @return true if the app is in foreground, false otherwise
     */
    private fun isAppInForeground(): Boolean {
        val isInForeground =
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        PushSDKLogger.debug(applicationContext, "App is in foreground: $isInForeground")
        return isInForeground
    }

    /**
     * Update device registration when a new token is received
     */
    private suspend fun updateDeviceRegistration(newToken: String) {
        coroutineScope {
            try {
                if (pushSdkSavedDataProvider.registrationStatus
                    && pushSdkSavedDataProvider.pushServiceRegistrationToken != ""
                    && pushSdkSavedDataProvider.firebaseRegistrationToken != ""
                ) {
                    val localPhoneInfoNewToken = Info.getDeviceType(applicationContext)
                    PushSDKLogger.debug(
                        applicationContext,
                        "${javaClass.simpleName}.onNewToken: localPhoneInfoNewToken: $localPhoneInfoNewToken"
                    )
                    val answerPlatform = apiHandler.updateRegistration(
                        pushSdkSavedDataProvider.pushServiceRegistrationToken,
                        pushSdkSavedDataProvider.firebaseRegistrationToken,
                        PushSDK.getSDKVersionName(),
                        newToken,
                        localPhoneInfoNewToken
                    )
                    PushSDKLogger.debug(
                        applicationContext,
                        "${javaClass.simpleName}.onNewToken: tried to update reg. info -> $answerPlatform"
                    )
                } else {
                    PushSDKLogger.debug(
                        applicationContext,
                        "${javaClass.simpleName}.onNewToken: update: failed"
                    )
                }
            } catch (e: Exception) {
                PushSDKLogger.debug(
                    applicationContext,
                    "${javaClass.simpleName}.onNewToken: update error: ${Log.getStackTraceString(e)}"
                )
            }
        }
    }

    /**
     * Send delivery report for a message
     */
    private fun sendMessageDeliveryReport(remoteMessage: RemoteMessage) {
        try {
            val message =
                Gson().fromJson(remoteMessage.data["message"], PushDataMessageModel::class.java)
            message?.let {
                if (pushSdkSavedDataProvider.firebaseRegistrationToken != ""
                    && pushSdkSavedDataProvider.pushServiceRegistrationToken != ""
                ) {
                    val pushAnswer = apiHandler.hMessageDr(
                        message.messageId,
                        pushSdkSavedDataProvider.firebaseRegistrationToken,
                        pushSdkSavedDataProvider.pushServiceRegistrationToken
                    )
                    PushSDKLogger.debug(
                        applicationContext,
                        "From Message Delivery Report: $pushAnswer"
                    )
                    PushSDKLogger.debug(
                        applicationContext,
                        "delivery report success: messageid ${remoteMessage.messageId.toString()}," +
                                " token: ${pushSdkSavedDataProvider.firebaseRegistrationToken}," +
                                " push_k_registration_token: ${pushSdkSavedDataProvider.pushServiceRegistrationToken}"
                    )
                } else {
                    PushSDKLogger.debug(
                        applicationContext,
                        "delivery report failed: messageid ${remoteMessage.messageId.toString()}," +
                                " token: ${pushSdkSavedDataProvider.firebaseRegistrationToken}," +
                                " push_k_registration_token: ${pushSdkSavedDataProvider.pushServiceRegistrationToken}"
                    )
                }
            }
        } catch (e: Exception) {
            PushSDKLogger.error("onMessageReceived: failed: ${Log.getStackTraceString(e)}")
            PushSDKLogger.debug(applicationContext, "remoteMessage: $remoteMessage")
        }
    }
}
