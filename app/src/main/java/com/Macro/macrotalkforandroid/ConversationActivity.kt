package com.Macro.macrotalkforandroid

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Macro.macrotalkforandroid.R

class ConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val list = findViewById<RecyclerView>(R.id.Conversation_DialogueList)
        list.setBackgroundColor(Color.parseColor(Utils.SettingData.ConversationBgColor))
    }
}