package com.gms_worldwide.push.app.handler

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gms_worldwide.push.app.R
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("PushBroadcastReceiver - onReceive is call, intent: $intent")
        if (intent != null && context != null) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            when (intent.action) {
                PushSDK.NOTIFICATION_REPLY_INTENT_ACTION -> {
                    intent.extras?.let {
                        //get extra data
                        val data = it.getString(PushSDK.NOTIFICATION_REPLY_DATA_EXTRA_NAME)
                        val notificationTag = it.getString(PushSDK.NOTIFICATION_TAG_EXTRA_NAME)
                        val notificationId = it.getInt(PushSDK.NOTIFICATION_ID_EXTRA_NAME)
                        println("data: $data")
                        println("tag: $notificationTag")
                        println("id: $notificationId")

                        if (remoteInput != null) {
                            //get reply text
                            val reply = remoteInput.getCharSequence(
                                PushSdkNotificationManager.REMOTE_INPUT_KEY
                            ).toString()
                            println("reply is: $reply")
                            updateNotification(
                                context,
                                notificationTag.toString(),
                                notificationId,
                                reply
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateNotification(context: Context, tag: String, id: Int, reply: String) {
/*        val notification = NotificationCompat.Builder(
            context,
            PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_not_icon)
            .setContentText(reply)
            .setTimeoutAfter(1)
            .build()

        // show notification. This hides direct reply UI
         NotificationManagerCompat.from(context).notify(id, notification)*/

        NotificationManagerCompat.from(context).cancel(tag, id)
    }
}