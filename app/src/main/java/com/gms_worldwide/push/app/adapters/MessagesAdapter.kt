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
import kotlinx.android.synthetic.main.message_item.view.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Compare two Message by time.
 */
@RequiresApi(Build.VERSION_CODES.O)
private val BY_DATE: Comparator<Message> = Comparator<Message> { o1, o2 ->
    val s1: Long = LocalDateTime.parse(o1.time.split(".")[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .toEpochSecond(ZoneOffset.UTC)
    val s2: Long = LocalDateTime.parse(o2.time.split(".")[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .toEpochSecond(ZoneOffset.UTC)
    (s2 - s1).toInt()
}


/**
 * MessagesAdapter is controller for message_item layout. Layout 'message_item' is used to show every message from the MutableList messages.
 * @param messages MutableList of Message
 * @param service Message and PushAnswerRegister service.
 * @param mainActivity
 * @param viewer RecyclerView of main activity layout.
 */
class MessagesAdapter(
    internal var messages: MutableList<Message>,
    private val service: Service,
    private val mainActivity: MainActivity,
    private val viewer: RecyclerView
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Called when ViewHolder creates.
     * @return instance of inner class MessageViewHolder. Contains 'message_item' layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.message_item,
                parent,
                false
            )
        )
    }

    /**
     * Called when ViewHolder binds.
     * Description of 'main_item' layout behavior.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        if (messages.size > 0) {
            Collections.sort(messages, BY_DATE)

            val message = messages[position]
            var messageAdapter: MessageAdapter
            holder.itemView.apply {
                title.text = message.title
                if (message.body.length > 35) {
                    body.text = message.body.substring(0, 35) + " ..."
                } else {
                    body.text = message.body
                }

                if (message.time != null) {
                    try {

                        time.text = getFormattedDate(message.time)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        time.text = message.time
                    }
                } else {
                    time.text = message.time

                }

                /**
                 * Use it in case when you collect messages in own DB, and not use message data from push server DB.
                 */
                /*delete_btn.setOnClickListener{
                    var deleteList : MutableList<Message>
                    deleteList = arrayListOf()
                    for (mss in messages) {
                        if (mss.id == message.id){
                            if (deleteMessage(mss)){
                                deleteList.add(mss)
                            }
                        }
                    }
                    messages.removeAll(deleteList)
                    notifyDataSetChanged()
                }*/

                message_item.setOnClickListener {
                    messageAdapter = MessageAdapter(message.messageId, service, messages)
                    viewer.adapter = messageAdapter
                    viewer.layoutManager = LinearLayoutManager(mainActivity)
                }

            }
        } else {
            holder.itemView.apply {
                title.text = "no data"
                body.text = "no data"
                time.text = ""
            }
        }
    }


    /**
     * Delete Message from local DB.
     * @param message Message.
     * @return Boolean.
     */
    private fun deleteMessage(message: Message): Boolean {
        return try {
            service.deleteMessage(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get item count. Returns needed count of 'message_item' layout.
     * In this case, returns size of Message List to show all messages.
     * If Message List is empty, returns 1.
     * @return Int.
     */
    override fun getItemCount(): Int {
        return if (messages.size > 0) {
            messages.size
        } else {
            1
        }
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
                timer = zonedUTC.withZoneSameInstant(ZoneId.of(timeZone.id)).toLocalDateTime()
                timer.format(DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm"))

            } catch (e: Exception) {
                e.printStackTrace()
                time
            }

        } else {
            time
        }


    }
}