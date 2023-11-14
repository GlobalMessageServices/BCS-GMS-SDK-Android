package com.gms_worldwide.push.app

import android.annotation.SuppressLint
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.gms_worldwide.push.app.databinding.ActivityMainBinding
import com.gms_worldwide.push.app.models.MessageList
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import java.util.*


class MainActivity : AppCompatActivity() {

    private val BROADCAST_PUSH_DATA_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"
    private val BROADCAST_PUSH_DATA_EXTRA_NAME = "data"
    private lateinit var textOut: TextView
    private lateinit var mainBinding: ActivityMainBinding

    private lateinit var testBuild: Notification
    private var testNotId = 123


    /**
     * Create broadcast receiver to catch single messages and messages from queue
     */
    private val mPlugInReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("onReceive is call, intent: $intent")
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            when (intent.action) {
                PushSDK.NOTIFICATION_CLICK_INTENT_ACTION -> {
                    intent.extras?.let {
                        Log.d(
                            "TAG1",
                            it.getString(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME).toString()
                        )
                        textOut.text =
                            it.getString(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME).toString()

                    }
                }

                BROADCAST_PUSH_DATA_INTENT_ACTION -> {
                    intent.extras?.let {
                        Log.d(
                            "TAG1",
                            it.getString(BROADCAST_PUSH_DATA_EXTRA_NAME).toString()
                        )
                        textOut.text = it.getString(BROADCAST_PUSH_DATA_EXTRA_NAME).toString()

                    }
                }

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
                                "pushsdk.remote_input_key"
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
        val notification = NotificationCompat.Builder(
            context,
            PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_not_icon)
            .setContentText(reply)
            .setTimeoutAfter(1)
            .build()

        // show notification. This hides direct reply UI
        // NotificationManagerCompat.from(context).notify(id, notification)


        NotificationManagerCompat.from(context).cancel(tag, id)
    }


    override fun onStart() {
        super.onStart()


        //Register broadcast receiver and add actions.

        try {
            unregisterReceiver(mPlugInReceiver)
        } catch (e: Exception) {

        }

        val filter = IntentFilter()
        filter.addAction(PushSDK.NOTIFICATION_CLICK_INTENT_ACTION)
        filter.addAction(BROADCAST_PUSH_DATA_INTENT_ACTION)
        filter.addAction(PushSDK.NOTIFICATION_REPLY_INTENT_ACTION)
        registerReceiver(mPlugInReceiver, filter)

    }

    override fun onPause() {
        super.onPause()

        //Unregister broadcast receiver to avoid duplicating of it.
        //unregisterReceiver(mPlugInReceiver)
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        //setContentView(R.layout.activity_main)

        var pushSDK = PushSDK(
            context = this,
            baseApiUrl = "base-api-url",
            PushSDK.LogLevels.PUSHSDK_LOG_LEVEL_DEBUG,
            enableAutoDeliveryReport = false
        )


        val clientAPI = "client-api-key"


        textOut = mainBinding.textOut

        var reg = mainBinding.reg
        var upd = mainBinding.update
        var getAll = mainBinding.getAll
        var history = mainBinding.history
        var queue = mainBinding.queue
        var clearCurrent = mainBinding.clearCurrent
        var clearAll = mainBinding.clearAll
        var dr = mainBinding.dr
        var callBack = mainBinding.callBack
        var clearText = mainBinding.clearText
        var dataBtn = mainBinding.dataBtn
        var viberBtn =mainBinding.viberBtn
        reg.setOnClickListener {
            var response = pushSDK.registerNewDevice(
                clientAPI,  //API key that you would be provided with
                "app-finger-print", //APP fingerprint that you would be provided with
                "1234567890", //Device's phone number
                "Android" //password, associated with Device's phone number (legacy - it is unused, you can put any value)
            )
            print(response)
            textOut.text = response.toString()
        }
        upd.setOnClickListener {
            var response = pushSDK.updateRegistration()
            print(response)
            textOut.text = response.toString()
        }
        getAll.setOnClickListener {
            var response = pushSDK.getAllRegisteredDevices()
            print(response)
            textOut.text = response.toString()
        }
        history.setOnClickListener {
            var response = pushSDK.getMessageHistory(604800)
            print(response)
            if (response.code == 200) {
                val messages = Gson().fromJson(response.body, MessageList::class.java)
                textOut.text = messages.toString()
            } else {
                textOut.text = response.toString()
            }
        }
        queue.setOnClickListener {
            var response = pushSDK.checkMessageQueue()
            print(response)
            textOut.text = response.toString()
        }
        clearCurrent.setOnClickListener {
            var response = pushSDK.unregisterCurrentDevice()
            print(response)
            textOut.text = response.toString()
        }
        clearAll.setOnClickListener {
            var response = pushSDK.unregisterAllDevices()
            print(response)
            textOut.text = response.toString()
        }
        dr.setOnClickListener {
            var response = pushSDK.sendMessageDeliveryReport("09876543-5461-11ed-1234-005056098cc1")
            print(response)
            textOut.text = response.toString()
        }
        callBack.setOnClickListener {
            var response =
                pushSDK.sendMessageCallback(
                    "09876543-5461-11ed-1234-005056098cc1",
                    "some callback text"
                )
            print(response)
            textOut.text = response.toString()
        }
        clearText.setOnClickListener {
            textOut.text = ""
        }

        dataBtn.setOnClickListener {
            val data = pushSDK.getUserData()
            println(data)
            textOut.text = data.toString()
        }



        viberBtn.setOnClickListener {
            //Example of keypad deep link string where 4790566326 is phone number
            val keypadUri = "viber://keypad?number=1234567890"
            //Create Uri from the keypad link
            val uri = Uri.parse(keypadUri)
            //Create ACTION_VIEW intent passing the uri to it
            val intent = Intent(Intent.ACTION_VIEW, uri)

            val isViberInstalled = isAppInstalled(this, "com.viber.voip")
            println(isViberInstalled)

            if (isViberInstalled) {
                println("Viber is installed")
                // Open Viber with the deep link
                startActivity(intent)

            } else {
                // Viber is not installed on the device, handle this case as needed
                println("Viber is not installed")
            }
        }
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    override fun onNewIntent(intent: Intent?) {
        println("onNewIntent: $intent")
        super.onNewIntent(intent)
    }
}

