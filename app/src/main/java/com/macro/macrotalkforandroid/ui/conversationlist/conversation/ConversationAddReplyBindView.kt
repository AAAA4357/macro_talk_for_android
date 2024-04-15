package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.marginTop
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.macro.macrotalkforandroid.R
import org.xml.sax.Attributes

//对话中添加回复视图绑定类，用于显示添加回复的对话框，并处理添加和移除回复的逻辑。

class ConversationAddReplyBindView(val context: Context) :
    OnBindView<BottomDialog>(R.layout.conversation_add_reply) {

    lateinit var view: View

    //在对话框绑定时调用，设置视图和点击事件监听器。
    override fun onBind(dialog: BottomDialog?, v: View?) {
        view = v!!
        val add = v.findViewById<ImageButton>(R.id.reply_add)
        add.setOnClickListener(OnAddReplyClick())
        val remove = v.findViewById<ImageButton>(R.id.reply_remove)
        remove.setOnClickListener(OnRemoveReplyClick())
    }

    //获取添加的回复内容列表。

    fun getContents(): List<String> {
        val list = view.findViewById<LinearLayout>(R.id.reply_list)
        val contents = list.children.map {
            try {
                (it as TextView).text.toString()
            } catch (_: Exception) {
                ""
            }
        }.toList()
        return contents.subList(0, contents.size - 1)
    }

    //点击添加回复按钮的点击事件监听器，动态添加一个回复输入框。

    inner class OnAddReplyClick : OnClickListener {
        override fun onClick(v: View?) {
            val reply = EditText(context, null, android.R.attr.editTextStyle, R.style.input_underline)
            reply.apply {
                inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                setTextAppearance(R.style.default_text_dark)
            }
            val list = view.findViewById<LinearLayout>(R.id.reply_list)
            list.addView(reply, list.childCount - 1)

            val remove = view.findViewById<ImageButton>(R.id.reply_remove)
            remove.visibility = View.VISIBLE
        }
    }

    //点击移除回复按钮的点击事件监听器，移除最后一个回复输入框。
    inner class OnRemoveReplyClick : OnClickListener {
        override fun onClick(v: View?) {
            val list = view.findViewById<LinearLayout>(R.id.reply_list)
            val count = list.childCount
            list.removeViewAt(count - 2)
            if (list.childCount == 1) {
                val remove = view.findViewById<ImageButton>(R.id.reply_remove)
                remove.visibility = View.GONE
            }
        }
    }
}
