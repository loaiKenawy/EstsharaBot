package com.example.estsharabot.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.estsharabot.databinding.LayoutReceiveBinding
import com.example.estsharabot.databinding.LayoutSendBinding
import com.example.estsharabot.model.Message

class LiveChatAdapter(private val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "LiveChatAdapter"
    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            // inflate receive
            ReceiveViewHolder(LayoutReceiveBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        } else {
            // inflate sent
            SentViewHolder(LayoutSendBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        if (holder.javaClass == SentViewHolder::class.java) {
            // do the stuff for sent view holder
            try {
                val viewHolder = holder as SentViewHolder
                holder.sentMessage.text = currentMessage.message
            } catch (e: java.lang.Exception) {
                Log.e(TAG, e.message.toString())
            }


        } else {
            // do stuff for receive view holder
            try {
                val viewHolder = holder as ReceiveViewHolder
                holder.receiveMessage.text = currentMessage.message
            } catch (e: java.lang.Exception) {
                Log.e(TAG, e.message.toString())
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (currentMessage.isUser) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private class ReceiveViewHolder(binding: LayoutReceiveBinding) : RecyclerView.ViewHolder(binding.root) {
        val receiveMessage = binding.receiveMessage

    }

    private class SentViewHolder(binding: LayoutSendBinding) : RecyclerView.ViewHolder(binding.root) {
        val sentMessage = binding.sendMessage

    }


}
