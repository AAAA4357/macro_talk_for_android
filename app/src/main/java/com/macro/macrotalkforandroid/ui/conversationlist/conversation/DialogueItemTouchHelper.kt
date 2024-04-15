package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.Utils
import java.util.Collections

// 拖拽帮助类，用于处理对话列表中的拖拽事件
class DialogueItemTouchHelper(
    activity: ConversationActivity,
    dialogues: List<Dialogue>,
    // 对话列表
    recycleViewAdapter: DialogueListAdapter,
    // 对话列表适配器
    val conversation: Conversation
    // 对话对象
) : ItemTouchHelper.Callback() {
    private val dialogues: List<Dialogue>
    private val recycleViewAdapter: DialogueListAdapter
    var activity: ConversationActivity

    init {
        this.dialogues = dialogues
        this.recycleViewAdapter = recycleViewAdapter
        this.activity = activity
    }

    // 获取拖拽和滑动的标志
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    // 处理拖拽事件
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        recyclerView.parent.requestDisallowInterceptTouchEvent(true)
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dialogues, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dialogues, i, i - 1)
            }
        }
        recycleViewAdapter.notifyItemMoved(fromPosition, toPosition)
        val dialogue = conversation.Dialogues[fromPosition]
        val targetDialogue = conversation.Dialogues[toPosition]
        conversation.Dialogues.removeAt(fromPosition)
        val targetIndex = conversation.Dialogues.indexOf(targetDialogue)
        conversation.Dialogues.add(if (fromPosition > toPosition) targetIndex else targetIndex + 1, dialogue)

        return true
    }

    // 处理滑动事件
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    // 启用长按拖拽
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
}
