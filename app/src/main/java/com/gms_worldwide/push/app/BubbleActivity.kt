package com.gms_worldwide.push.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gms_worldwide.push.app.databinding.ActivityBubbleBinding
import com.gms_worldwide.push.app.databinding.ItemMessageBinding
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK


class BubbleActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter

    private lateinit var bubleBinding: ActivityBubbleBinding
    private lateinit var messageItemBinding: ItemMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bubleBinding = ActivityBubbleBinding.inflate(layoutInflater)
        messageItemBinding = ItemMessageBinding.inflate(layoutInflater)
        setContentView(bubleBinding.root)
        //setContentView(R.layout.activity_bubble)


        val messagesList: MutableList<ChatMessage> = ArrayList()

        val messagesElement =  bubleBinding.messages
        val sendElement =  bubleBinding.send
        val inputElement =  bubleBinding.input
        val itemLayout = messageItemBinding.itemLayout
        val messageItem = messageItemBinding.messageItem


        messageAdapter = MessageAdapter(messagesList, itemLayout, messageItem)
        messagesElement.adapter = messageAdapter
        messagesElement.layoutManager = LinearLayoutManager(this)


        sendElement.setOnClickListener {
            messagesList.add(ChatMessage(inputElement.text.toString(), false))
            inputElement.text.clear()
        }


        //Get bubble extra data (push message) from intent
        intent.extras?.let{
            val extra = it.getString(PushSDK.NOTIFICATION_BUBBLES_PUSH_DATA_EXTRA_NAME)
            val message = Gson().fromJson(extra, com.gms_worldwide.push.app.models.PushDataMessageModel::class.java)
            messagesList.add(ChatMessage(message.body, true))
        }

    }


    override fun onNewIntent(intent: Intent?) {
        println("onNewIntent: $intent")
        super.onNewIntent(intent)
    }

}


data class ChatMessage(
    val text: String,
    val isIncoming: Boolean

)

