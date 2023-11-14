package com.gms_worldwide.push.app

import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gms_worldwide.push.app.adapters.*
import com.gms_worldwide.push.app.db.DBHelper
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.service.Service
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.test_item.*
import kotlinx.android.synthetic.main.test_item.view.*


private lateinit var authAdapter: AuthAdapter
private lateinit var messagesAdapter: MessagesAdapter
private lateinit var mainAdapter: MainAdapter
private lateinit var testAdapter: TestAdapter
private lateinit var service: Service
private lateinit var pushSDK: PushSDK
private lateinit var messages: MutableList<Message>

class MainActivity : AppCompatActivity() {


    /**
     * Create broadcast receiver to catch single messages and messages from queue
     */
    private val mPlugInReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PushSDK.BROADCAST_PUSH_DATA_INTENT_ACTION -> {
                    intent.extras?.let {
                        Log.d(
                            "TAG1",
                            it.getString(PushSDK.BROADCAST_PUSH_DATA_EXTRA_NAME).toString()
                        )

                        val messageStr =
                            it.getString(PushSDK.BROADCAST_PUSH_DATA_EXTRA_NAME).toString()

                        if (messageStr.isNotEmpty()) {
                            val message = try {
                                Gson().fromJson(messageStr, Message::class.java)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (message != null && message is Message) {

                                if (!service.isMessageAlreadyInDB(message) && !isMessageAlreadyInList(message)) {
                                    try {
                                        //Use service.saveMessage(message) in case you collect messages in own DB, and not use message data from Hyber DB.
                                        //service.saveMessage(message)


                                        messages.add(message)
                                        messagesAdapter.notifyItemRangeRemoved(
                                            0,
                                            messagesAdapter.messages.size - 1
                                        );
                                        messagesAdapter.notifyItemRangeInserted(
                                            0,
                                            messagesAdapter.messages.size - 1
                                        )
                                        mainAdapter.notifyItemRangeRemoved(0, 1)
                                        mainAdapter.notifyItemRangeInserted(0, 1)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
                PushSDK.BROADCAST_QUEUE_INTENT_ACTION -> {
                    intent.extras?.let {
                        Log.d("TAG2", it.getString(PushSDK.BROADCAST_QUEUE_EXTRA_NAME).toString())
                    }
                }


            }
        }
    }


    private fun isMessageAlreadyInList(message: Message): Boolean {
        var result = false

        for (mss in messages) {
            if (message.messageId.equals(mss.messageId)) {
                return true
            }
        }

        return result
    }



    override fun onStart() {
        super.onStart()


        //Register broadcast receiver and add actions.
        val filter = IntentFilter()
        filter.addAction(PushSDK.BROADCAST_PUSH_DATA_INTENT_ACTION)
        filter.addAction(PushSDK.BROADCAST_QUEUE_INTENT_ACTION)
        //filter.addAction(PushSDK.NOTIFICATION_CLICK_INTENT_ACTION)
        registerReceiver(mPlugInReceiver, filter)

    }

    override fun onPause() {
        super.onPause()

        //Unregister broadcast receiver to avoid duplicating of it.
        unregisterReceiver(mPlugInReceiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


            setInternalData()

            intent?.let {
                if (it.action == PushSDK.NOTIFICATION_CLICK_INTENT_ACTION) {
                    it.extras?.apply {
                        Log.d("TAG3", getString(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME).toString())
                    }
                }
            }

            if (!isDeviceRegistered()) {
                authAdapter = AuthAdapter(this, mainAdapter, main_view, pushSDK, service)

                main_view.adapter = authAdapter

            } else {
                main_view.adapter = mainAdapter
            }

        main_view.layoutManager = LinearLayoutManager(this)

    }

    fun isDeviceRegistered() : Boolean{

        var result = false
        var response = pushSDK.getAllRegisteredDevices()

        if (response.code == 200){
            result = true
        }

        return result
    }

    private val finishApp = {
        dialog: DialogInterface, which: Int ->
        finishAffinity()
    }

    /**
     * Called when back button pressed
     */
    override fun onBackPressed() {
        if (main_view.adapter is MessageAdapter) {
            main_view.adapter = messagesAdapter
            main_view.layoutManager = LinearLayoutManager(this)
        } else {
            if (main_view.adapter is MessagesAdapter) {
                main_view.adapter = mainAdapter
                main_view.layoutManager = LinearLayoutManager(this)
            } else {
                if (main_view.adapter is AuthAdapter && isDeviceRegistered()) {
                    main_view.adapter = mainAdapter
                    main_view.layoutManager = LinearLayoutManager(this)
                } else {
                    if (main_view.adapter is TestAdapter) {
                        main_view.adapter = mainAdapter
                        main_view.layoutManager = LinearLayoutManager(this)
                    } else {
                        super.onBackPressed()
                    }

                }
            }
        }
    }


    /**
     * Set up application.
     */
    private fun setInternalData() {
        //Use service.getMessages() in case you collect messages in own DB, and not use message data from push server DB.
        val dbHelper = DBHelper(this)
        service = Service(dbHelper)
        //messages = service.getMessages()


        pushSDK = PushSDK(
            context = this,
            baseApiUrl = "base-api-url",
            PushSDK.LogLevels.PUSHSDK_LOG_LEVEL_DEBUG
        )

        //Get message data from push server DB. Use it when you do not collect messages in own DB.
        messages = getMessages(864000)


        testAdapter = TestAdapter(pushSDK)
        messagesAdapter = MessagesAdapter(messages, service, this, main_view)
        mainAdapter =
            MainAdapter(messages, messagesAdapter, this, main_view, pushSDK, service, testAdapter)
    }



    /**
     * Get message history. Returns messages from push server DB.
     * @return MutableList of messages.
     */
    private fun getMessages(seconds: Int): MutableList<Message> {
        var result: Array<Message> = emptyArray()
        var ansverFromPushServer = pushSDK.getMessageHistory(seconds)

        if (ansverFromPushServer.code == 200) {
            try {

                var messagesStr = ansverFromPushServer.body

                messagesStr = messagesStr.split("\"messages\":")[1].trim()

                messagesStr = messagesStr.substring(0, messagesStr.length - 1)

                result = Gson().fromJson(messagesStr, Array<Message>::class.java)
            } catch (e: Exception) {
                e.printStackTrace()

            }

        }

        return result.toMutableList()
    }






}