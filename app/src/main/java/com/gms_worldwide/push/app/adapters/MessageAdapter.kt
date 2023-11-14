package com.gms_worldwide.push.app.adapters

import android.icu.util.TimeZone
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.gms_worldwide.push.app.R
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.service.Service
import kotlinx.android.synthetic.main.message.view.message_single
import kotlinx.android.synthetic.main.message.view.time_single
import kotlinx.android.synthetic.main.message.view.title_single
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * MessageAdapter is controller for message layout.
 * @param message_id message_id of displayed Message.
 * @param messages MutableList of Message
 * @param service Message and PushAnswerRegister service.
 */
class MessageAdapter(private var message_id: String, private var service: Service, private var messages : MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageHolder>() {

    class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Called when ViewHolder creates.
     * @return instance of inner class MessageHolder. Contains message layout.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageHolder {
        return MessageHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.message,
                parent,
                false
            )
        )
    }


    /**
     * Get item count. Returns needed count of 'message' layout. Layout 'message' is used to show one specified message.
     * In this case, returns 1, as we need only one item of 'message' layout.
     * @return Int.
     */
    override fun getItemCount(): Int {
        return 1;
    }



    /**
     * Called when ViewHolder binds.
     * Description of message layout behavior.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.itemView.apply {
           // var message = service.getMessageById(message_id)
            var message = getMessageById(message_id)

            title_single.text = message.title
            message_single.text = message.body
            time_single.text = getFormattedDate(message.time)
        }
    }

    /**
     * Get Message by message_id.
     * @param messageId message_id of Message.
     * @return Message.
     */
    private fun getMessageById(messageId: String): Message {
        var result = Message("","","","","")
        for (message in messages) {
            if (message.messageId.equals(messageId)){
                result = message
            }
        }
        return result
    }

    /**
     * Get formatted date. For example: from string date 2022-05-17T12:47:12.595111+00 it makes string date 17 May 2020 12:47.
     * @param time String.
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


}