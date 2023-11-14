package com.gms_worldwide.push.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val messages: MutableList<ChatMessage>,
    private val item_layout: LinearLayout,
    private val message_item: TextView
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_message,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messages[position]


        holder.itemView.apply {
            item_layout.setPadding(16, 16, 16, 16)
            if (!currentMessage.isIncoming) {
                message_item.gravity = Gravity.END
                item_layout.gravity = Gravity.END
            }
            message_item.text = currentMessage.text

        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}