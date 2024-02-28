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

class DialogueItemTouchHelper(
    activity: ConversationActivity,
    dialogues: List<Dialogue>,
    recycleViewAdapter: DialogueListAdapter,
    val conversation : Conversation
) : ItemTouchHelper.Callback() {
    private val dialogues: List<Dialogue>
    private val recycleViewAdapter: DialogueListAdapter
    var activity: ConversationActivity

    init {
        this.dialogues = dialogues
        this.recycleViewAdapter = recycleViewAdapter
        this.activity = activity
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

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
        conversation.Dialogues.add(targetIndex, dialogue)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
}