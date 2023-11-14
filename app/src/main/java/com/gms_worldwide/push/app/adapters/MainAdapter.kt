package com.gms_worldwide.push.app.adapters

import android.icu.util.TimeZone
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gms_worldwide.push.app.MainActivity
import com.gms_worldwide.push.app.R
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.service.Service
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.push.android.pushsdkandroid.PushSDK
import kotlinx.android.synthetic.main.main.view.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * MainAdapter is controller for 'main' layout. Layout 'main' is used to show last message and other functions - check queue,
 * message history and change number.
 * @param messages MutableList of Message
 * @param messagesAdapter controller of messages page.
 * @param mainActivity
 * @param viewer RecyclerView of main activity layout.
 * @param pushSDK instance of PushSDK from Android SDK.
 * @param service Message and PushAnswerRegister service.
 */

class MainAdapter(
    internal var messages: MutableList<Message>,
    internal val messagesAdapter: MessagesAdapter,
    private val mainActivity: MainActivity,
    private val viewer: RecyclerView,
    private val pushSDK: PushSDK,
    private val service: Service,
    private val testAdapter: TestAdapter

) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {


    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Called when ViewHolder creates.
     * @return instance of inner class MainViewHolder. Contains 'main' layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.main,
                parent,
                false
            )
        )
    }


    /**
     * Called when ViewHolder binds.
     * Description of 'main' layout behavior.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val message = getLastMessage(messages)
        holder.itemView.apply {

            if (message.time.isEmpty() && message.title.isEmpty() && message.body.isEmpty()) {
                time_single.text = "no data"
                title_single.text = "no data"
                message_single.text = "no data"
            } else {
                time_single.text = getFormattedDate(message.time)
                title_single.text = message.title
                message_single.text = message.body
            }


            history.setOnClickListener {
                viewer.adapter = messagesAdapter
                viewer.layoutManager = LinearLayoutManager(mainActivity)

            }

            new_number.setOnClickListener {
                val authAdapter = AuthAdapter(
                    mainActivity, this@MainAdapter, viewer,
                    pushSDK, service
                )
                viewer.adapter = authAdapter
                viewer.layoutManager = LinearLayoutManager(mainActivity)
            }

            queue.setOnClickListener {

                var response = pushSDK.checkMessageQueue()
                try {
                    var messagesStr = response.body
                    val type = object : TypeToken<MutableMap<String, Array<Message>>>() {}.type

                    val map: Map<String, Array<Message>> = Gson().fromJson(messagesStr, type)

                    var messagesList = map["messages"]
                    if (messagesList != null) {

                        if (!messagesList.isEmpty()) {
                            for (mss in messagesList) {
                                pushSDK.sendMessageDeliveryReport(mss.messageId)
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            testBtn.setOnClickListener {
                viewer.adapter = testAdapter
                viewer.layoutManager = LinearLayoutManager(mainActivity)
            }

        }
    }

    /**
     * Get item count. Returns needed count of 'main' layout.
     * In this case, returns 1, as we need only one item of 'main' layout.
     * @return Int.
     */
    override fun getItemCount(): Int {
        return 1;
    }

    /**
     * Get formatted date. For example: from string date 2022-05-17T12:47:12.595111+00 it makes string date 17 May 2020 12:47.
     * @param time String
     * @return String.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(time: String): String {

        return if (time != null && time.trim().isNotEmpty()) {
            try {
                val timeStr = time.split(".")[0]
                var timer = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val timeZone = TimeZone.getDefault()
                val zonedUTC = timer.atZone(ZoneId.of("UTC"))
                timer = zonedUTC.withZoneSameInstant(ZoneId.of(timeZone.getID())).toLocalDateTime()
                timer.format(DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm"))

            } catch (e: Exception) {
                e.printStackTrace()
                time
            }

        } else {
            time
        }


    }

    /**
     * Get last Message from List of Message.
     * @param messages List of Message.
     * @return last received Message.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessage(messages: List<Message>): Message {
        var result = Message("", "", "", "", "");
        var lastDate = LocalDateTime.of(1990, 1, 1, 0, 0)
        for (message in messages) {
            val timeStr = message.time.split(".")[0]
            var date = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            if (lastDate.isBefore(date)) {
                result = message
                lastDate = date
            }

        }

        return result;
    }
}