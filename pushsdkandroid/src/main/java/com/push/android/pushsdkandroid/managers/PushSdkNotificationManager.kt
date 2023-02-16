package com.push.android.pushsdkandroid.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.models.PushDataMessageModel
import com.push.android.pushsdkandroid.settings.BubbleSettings
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.random.Random


/**
 * The SDK's notification manager; Used to display notifications and check availability
 *
 * @constructor The SDK's notification manager
 *
 * @param context Context to use
 * @param summaryNotificationTitleAndText Summary notification title and text <title, text>,
 * used for displaying a "summary notification", which serves as a root notification for other notifications
 * notifications will not be bundled(grouped) if null; Ignored if api level is below android 7
 * @param notificationIconResourceId Notification small icon
 * @param bubbleIconResourceId Bubble icon
 */
class PushSdkNotificationManager(
    private val context: Context,
    private val summaryNotificationTitleAndText: Pair<String, String>?,
    private val notificationIconResourceId: Int = android.R.drawable.ic_notification_overlay,
    private val bubbleIconResourceId: Int = android.R.drawable.ic_dialog_email
) {
    /**
     * Notification constants
     */
    companion object {
        /**
         * max notifications that can be shown by the system at a time
         */
        const val MAX_NOTIFICATIONS = 25

        /**
         * Channel id of notifications
         */
        const val DEFAULT_NOTIFICATION_CHANNEL_ID = "pushsdk.notification.channel"

        /**
         * group id of notifications
         */
        const val DEFAULT_NOTIFICATION_GROUP_ID = "pushsdk.notification.group"

        /**
         * The "user-visible" name of the channel
         */
        const val NOTIFICATION_CHANNEL_NAME = "PushSDK channel"

        /**
         * tag for regular notification
         */
        const val NOTIFICATION_TAG = "pushsdk_b_n"

        /**
         * constant summary notification id
         */
        const val DEFAULT_SUMMARY_NOTIFICATION_ID = 0

        /**
         * tag for summary notification
         */
        const val SUMMARY_NOTIFICATION_TAG = "pushsdk_s_b_n"

        /**
         * shortcut category for bubble notification
         */
        const val NOTIFICATION_SHORTCUT_CATEGORY =
            "com.push.android.pushsdkandroid.default_category"

        /**
         * shortcut id for bubble notification
         */
        const val NOTIFICATION_SHORTCUT_ID = "com.push.android.pushsdkandroid.shortcut_id"

        /**
         * key for remote input
         */

        const val REMOTE_INPUT_KEY = "pushsdk.remote_input_key"
    }

    /**
     * Notification styles enumeration, used for displaying notifications
     * @see sendNotification
     */
    enum class NotificationStyle {
        /**
         * Shows notification without a style;
         * Text will be displayed as single line;
         * Will display the picture as large icon if push message has one
         */
        NO_STYLE,

        /**
         * Default style (Recommended);
         * Sets "Big text" style to allow multiple lines of text;
         * Will display the picture as large icon if push message has one
         */
        BIG_TEXT,

        /**
         * Shows image as big picture;
         * Or uses default style (no style) if image can not be displayed
         */
        BIG_PICTURE,

        /**
         * Shows notification as bubble
         * Uses default style (no style) if bubble can not be displayed
         */
        BUBBLES
    }

    /**
     * Get bitmap from a URL
     * @param strURL URL containing an image
     */
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        strURL?.let {
            try {
                URL(strURL).openConnection().run {
                    doInput = true
                    connect()
                    return BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                //not an error
                PushSDKLogger.debug(context, Log.getStackTraceString(e))
                return null
            }
        } ?: return null
    }

    /**
     * Constructs the base NotificationCompat Builder object for chaining
     * @param data - FCM push RemoteMessage's data
     * @param notificationStyle - which built-in style to use (BIG_TEXT as default)
     * @return NotificationCompat Builder object or null if message could not be read
     */
    fun constructNotification(
        data: Map<String, String>,
        notificationId: Int,
        notificationStyle: NotificationStyle,
        bubbleIntent: Intent? = null,
        bubbleSettings: BubbleSettings = BubbleSettings()
    ): NotificationCompat.Builder? {

        try {
            //parse the data object
            val message = Gson().fromJson(data["message"], PushDataMessageModel::class.java)
            if (message == null) {
                //message is empty, thus it must be an error
                PushSDKLogger.error("constructNotification - message is empty")
                //TODO do something if needed
                //then stop executing
                return null
            }

            var builder =
                NotificationCompat.Builder(
                    context.applicationContext,
                    DEFAULT_NOTIFICATION_CHANNEL_ID
                ).apply {
                    setGroup(DEFAULT_NOTIFICATION_GROUP_ID)
                    priority = NotificationCompat.PRIORITY_MAX

                    setAutoCancel(true)
                    setContentTitle(message.title)
                    setContentText(message.body)
                    setSmallIcon(notificationIconResourceId)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                    if (message.button != null) {
                        val btnText = message.button.text
                        val btnURL = message.button.url
                        if (btnText != null && btnText.isNotEmpty() && btnURL != null && btnURL.isNotEmpty()) {
                            //an intent for action button
                            val browserIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(btnURL))
                            val btnPendingIntent = PendingIntent.getActivity(
                                context,
                                1,
                                browserIntent,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                            addAction(
                                android.R.drawable.btn_default_small,
                                btnText,
                                btnPendingIntent
                            )
                        }
                    }

                    if (message.is2Way && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        var replyLabel = "Reply"
                        var remoteInput: RemoteInput = RemoteInput.Builder(REMOTE_INPUT_KEY).run {
                            setLabel(replyLabel)
                            build()
                        }
                        // Build a PendingIntent for the reply action to trigger.
                        var replyIntent = Intent()
                        replyIntent.action = PushSDK.NOTIFICATION_REPLY_INTENT_ACTION
                        replyIntent.putExtra(
                            PushSDK.NOTIFICATION_REPLY_DATA_EXTRA_NAME,
                            message.messageId
                        )
                        replyIntent.putExtra(
                            PushSDK.NOTIFICATION_TAG_EXTRA_NAME,
                            getNotificationTag()
                        )
                        replyIntent.putExtra(
                            PushSDK.NOTIFICATION_ID_EXTRA_NAME,
                            notificationId
                        )

                        var replyPendingIntent: PendingIntent =
                            PendingIntent.getBroadcast(
                                context,
                                3,
                                replyIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                            )

                        // Create the reply action and add the remote input.
                        var replyAction: NotificationCompat.Action =
                            NotificationCompat.Action.Builder(
                                android.R.drawable.ic_input_add,
                                replyLabel, replyPendingIntent
                            )
                                .addRemoteInput(remoteInput)
                                .build()

                        addAction(replyAction)
                    }

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        context.packageManager.getLaunchIntentForPackage(context.applicationInfo.packageName)
                            ?.let {
                                //build an intent for notification (click to open the app)
                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    0,

                                    it.apply {
                                        action = PushSDK.NOTIFICATION_CLICK_INTENT_ACTION
                                        putExtra(
                                            PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME,
                                            data["message"]

                                        )
                                    },
                                    PendingIntent.FLAG_IMMUTABLE,
                                )
                                setContentIntent(pendingIntent)
                            }

                        when (notificationStyle) {
                            NotificationStyle.NO_STYLE -> {
                                //image size is recommended to be <1mb for notifications
                                getBitmapFromURL(message.image.url)?.let {
                                    setLargeIcon(it)
                                }
                            }
                            NotificationStyle.BIG_TEXT -> {
                                setStyle(NotificationCompat.BigTextStyle())
                                //image size is recommended to be <1mb for notifications
                                getBitmapFromURL(message.image.url)?.let {
                                    setLargeIcon(it)
                                }
                            }
                            NotificationStyle.BIG_PICTURE -> {
                                //image size is recommended to be <1mb for notifications
                                getBitmapFromURL(message.image.url)?.let {
                                    setStyle(
                                        NotificationCompat.BigPictureStyle().bigPicture(it)
                                    )
                                }
                            }
                            NotificationStyle.BUBBLES -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    if (!setBubble(
                                            this,
                                            message,
                                            data,
                                            bubbleIntent,
                                            bubbleSettings
                                        )
                                    ) {
                                        getBitmapFromURL(message.image.url)?.let {
                                            setLargeIcon(it)
                                        }
                                    }
                                } else {
                                    getBitmapFromURL(message.image.url)?.let {
                                        setLargeIcon(it)
                                    }
                                }
                            }
                        }
                    } else {
                        setGroupSummary(true)
                    }
                }

            return builder
        } catch (e: Exception) {
            e.printStackTrace()
            PushSDKLogger.debug(context, "data: $data")
            return null
        }
    }


    /**
     * Constructs bubble
     * @param builder - notification builder the bubble will be added to
     * @param message - push message PushDataMessageModel
     * @param data - FCM push RemoteMessage's data
     * @param bubbleIntent - intent with BubbleActivity, it is used for creating pending intent for BubbleMetadata
     * @param bubbleSettings - settings for objects Person, ShortcutInfoCompat and BubbleMetadata
     * @return Boolean whether the bubble was added to notification builder
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun setBubble(
        builder: NotificationCompat.Builder,
        message: PushDataMessageModel,
        data: Map<String, String>,
        bubbleIntent: Intent?,
        bubbleSettings: BubbleSettings?
    ): Boolean {
        if (bubbleIntent != null && bubbleSettings != null) {
            bubbleIntent.setAction(Intent.ACTION_VIEW)
                .putExtra(
                    PushSDK.NOTIFICATION_BUBBLES_PUSH_DATA_EXTRA_NAME,
                    data["message"]
                )


            val icon: IconCompat = if (bubbleSettings.isDefaultBubbleIconUsed) {
                IconCompat.createWithResource(context, bubbleIconResourceId)
            } else {
                val imageIcon = getBitmapFromURL(message.image.url)
                if (imageIcon != null) {
                    IconCompat.createWithAdaptiveBitmap(imageIcon)
                } else {
                    IconCompat.createWithResource(context, bubbleIconResourceId)
                }
            }


            val person = Person.Builder()
                .setName(message.title)
                .setImportant(bubbleSettings.isImportant)
                .setIcon(icon)
                .build()

            val locusId = LocusIdCompat("com.push.android.pushsdkandroid_1")
            val shortcut =
                ShortcutInfoCompat.Builder(context, NOTIFICATION_SHORTCUT_ID)
                    .setLocusId(locusId)
                    .setShortLabel(bubbleSettings.shortLabel)
                    .setIcon(icon)
                    .setIsConversation()
                    .setLongLived(bubbleSettings.isLongLived)
                    .setCategories(setOf(NOTIFICATION_SHORTCUT_CATEGORY))
                    .setIntent(bubbleIntent)
                    .setPerson(person)
                    .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)

            val messagingStyle = NotificationCompat.MessagingStyle(person)
            val message2 = NotificationCompat.MessagingStyle.Message(
                message.body, LocalDateTime.now().toEpochSecond(
                    ZoneOffset.UTC
                ), person
            )
            messagingStyle.addMessage(message2)

            val bubblePendingIntent = PendingIntent.getActivity(
                context,
                2,
                bubbleIntent,
                flagUpdateCurrent(true)
            )
            val babbleData = NotificationCompat.BubbleMetadata.Builder(
                bubblePendingIntent,
                icon
            )
                .setDesiredHeight(bubbleSettings.desiredHeight)
                .setAutoExpandBubble(bubbleSettings.isAutoExpandBubble)
                .setSuppressNotification(bubbleSettings.isSuppressNotification)
                .build()

            builder.setShortcutId(NOTIFICATION_SHORTCUT_ID)
            builder.bubbleMetadata = babbleData
            builder.setLocusId(locusId)
            builder.addPerson(person)
            builder.setStyle(messagingStyle)
            return true
        }
        return false

    }


    /**
     * @param mutable
     * @return pending intent flag, depending on the api level
     */
    private fun flagUpdateCurrent(mutable: Boolean): Int {
        return if (mutable) {
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
    }


    /**
     * Build and show the notification
     *
     * @param notificationConstruct NotificationCompat.Builder object to send
     */
    fun sendNotification(
        notificationConstruct: NotificationCompat.Builder,
        notificationId: Int
    ): Boolean {
        try {

            /*val notificationId = Random.nextInt(
                DEFAULT_SUMMARY_NOTIFICATION_ID + 1,
                Int.MAX_VALUE - 10
            )*/
            val notification = notificationConstruct.build()
            NotificationManagerCompat.from(context.applicationContext).apply {
                //Create notification channel if it doesn't exist (mandatory for Android O and above)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationChannel = NotificationChannel(
                        DEFAULT_NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        notificationChannel.setAllowBubbles(true)
                    }

                    createNotificationChannel(
                        notificationChannel
                    )
                }

                //show notification
                notify(
                    getNotificationTag(),
                    notificationId,
                    notification
                )

            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Whether system has space to show at least 1 more notification;
     * Assume there is no space by default; Will always return true for api levels < 23
     *
     * @param cancelOldest - cancel oldest notification to free up space
     */
    fun hasSpaceForNotification(cancelOldest: Boolean): Boolean {
        var hasSpaceForNotification = false

        //check notification limit, and cancel first active notification if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
            val activeNotifications = notificationManager.activeNotifications.toMutableList()
            val compareNotificationByPostTime = Comparator<StatusBarNotification> { o1, o2 ->
                return@Comparator (o1.postTime).compareTo(o2.postTime)
            }
            when (activeNotifications.size) {
                in 0 until MAX_NOTIFICATIONS -> {
                    hasSpaceForNotification = true
                }
                else -> {
                    if (cancelOldest) {
                        Collections.sort(activeNotifications, compareNotificationByPostTime)
                        for (activeNotification in activeNotifications) {
                            if (activeNotification.tag == NOTIFICATION_TAG) {
                                notificationManager.cancel(
                                    activeNotification.tag, activeNotification.id
                                )
                                hasSpaceForNotification = true
                                break
                            }
                        }
                    }
                }
            }
        } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //TODO not yet supported, might use NotificationListenerService
                //assume there is space
                hasSpaceForNotification = true
            } else {
                //assume there is space, nothing can be done in this case
                hasSpaceForNotification = true
            }

        return hasSpaceForNotification
    }

    /**
     * Whether notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
    }

    /**
     * Whether user has "Do not disturb mode on"; Will return false if unable to obtain information
     */
    fun isDoNotDisturbModeActive(): Boolean {
        return try {
            val notificationManager =
                context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PushSDKLogger.debug(
                    context.applicationContext,
                    "currentInterruptionFilter: ${notificationManager.currentInterruptionFilter}"
                )
                notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Whether notification channel is muted on the user's device
     * @param channelId channel id to check
     */
    @Suppress("LiftReturnOrAssignment")
    fun isNotificationChannelMuted(channelId: String): Boolean {
        val notificationManager =
            context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel != null) {
                val importance = channel.importance
                PushSDKLogger.debug(
                    context.applicationContext,
                    "getNotificationChannel(channelId).importance: $importance"
                )
                return importance == NotificationManager.IMPORTANCE_NONE
            } else {
                PushSDKLogger.debug(
                    context.applicationContext,
                    "$channelId - CHANNEL ID DOES NOT EXIST"
                )
                return false
            }
        } else {
            return false
        }
    }

    /**
     * @return notification tag depend on API
     */
    private fun getNotificationTag(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            NOTIFICATION_TAG
        } else {
            SUMMARY_NOTIFICATION_TAG
        }
    }

    /**
     * @return notification tag depend on API
     */
    fun getNotificationId(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Random.nextInt(
                DEFAULT_SUMMARY_NOTIFICATION_ID + 1,
                Int.MAX_VALUE - 10
            )
        } else {
            DEFAULT_SUMMARY_NOTIFICATION_ID
        }
    }
}