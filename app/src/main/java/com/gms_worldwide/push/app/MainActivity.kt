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
import androidx.core.app.RemoteInput
import com.gms_worldwide.push.app.databinding.ActivityMainBinding
import com.gms_worldwide.push.app.models.MessageList
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK


class MainActivity : AppCompatActivity() {


    private val BROADCAST_PUSH_DATA_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"
    private val BROADCAST_PUSH_DATA_EXTRA_NAME = "data"
    private lateinit var textOut: TextView
    private lateinit var mainBinding: ActivityMainBinding

    private lateinit var testBuild: Notification
    private var testNotId = 123


    /**
     * Create broadcast receiver to catch single messages
     */
    private val mPlugInReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("onReceive is call, intent: $intent")
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            when (intent.action) {

                BROADCAST_PUSH_DATA_INTENT_ACTION -> {
                    intent.extras?.let {
                        Log.d(
                            "TAG1",
                            "push message is ${
                                it.getString(BROADCAST_PUSH_DATA_EXTRA_NAME).toString()
                            }"
                        )
                        textOut.text = it.getString(BROADCAST_PUSH_DATA_EXTRA_NAME).toString()

                    }
                }
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mPlugInReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mPlugInReceiver, filter)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        //setContentView(R.layout.activity_main)

        /*val pushSDK = PushSDK(
            context = this,
            baseApiUrl = "base-api-url",
            PushSDK.LogLevels.PUSHSDK_LOG_LEVEL_DEBUG,
            enableAutoDeliveryReport = false
        )*/

        var pushSDK = PushSDK(
            context = this,
            baseApiUrl = "https://push.hyber.im/api/3.0",
            PushSDK.LogLevels.PUSHSDK_LOG_LEVEL_DEBUG,
            enableAutoDeliveryReport = false
        )
        R.drawable.ic_not_icon

        intent.extras?.let {
            if (!it.isEmpty) {
                when (intent.action) {
                    PushSDK.NOTIFICATION_CLICK_INTENT_ACTION -> {
                        val extra = it.getString(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME)
                        val message = Gson().fromJson(
                            extra,
                            com.gms_worldwide.push.app.models.PushDataMessageModel::class.java
                        )
                        println("message from click $message")
                    }
                }
            }
        }


        //val clientAPI = "client-api-key"
        val clientAPI = "dbb84261-f822-4ff3-aaf4-d4d9a62f109c"
        //val appFingerPrint = "app-finger-print"
        val appFingerPrint = "E9:BC:98:65:9A:83:FE:8A:10:AA:BB:D0:55:39:A6:15:86:40:C1:60"


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
        var viberBtn = mainBinding.viberBtn
        reg.setOnClickListener {
            var response = pushSDK.registerNewDevice(
                clientAPI,  //API key that you would be provided with
                appFingerPrint, //APP fingerprint that you would be provided with
                "380936328201", //Device's phone number
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

