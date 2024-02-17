package com.Macro.macrotalkforandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.Macro.macrotalkforandroid.R

class ConversationActivity : AppCompatActivity() {
    lateinit var conversationList : MutableList<Conversation>

    lateinit var conversationAdapter : ConversationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        conversationList = Utils.storageData.Conversations

        conversationAdapter = ConversationListAdapter(this@ConversationActivity, conversationList.toList())
    }

    override fun onStart() {
        super.onStart()

        val list = findViewById<RecyclerView>(R.id.conversation_list)
        val empty = findViewById<TextView>(R.id.conversation_empty)
        if (conversationList.isEmpty()) {
            empty.visibility = View.VISIBLE
        }
        else {
            empty.visibility = View.INVISIBLE
        }


    }

    fun addConversation(conversation : Conversation) {
        conversationList.add(conversation)
        conversationAdapter.addItem(conversation)
        val empty = findViewById<TextView>(R.id.profile_empty)
        empty.visibility = View.INVISIBLE
    }

    fun removeConversation() {

    }

    fun replaceConversation() {

    }
}