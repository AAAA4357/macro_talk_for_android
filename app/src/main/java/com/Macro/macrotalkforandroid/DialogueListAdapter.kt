package com.Macro.macrotalkforandroid

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Macro.macrotalkforandroid.R

class DialogueListAdapter(private val context: Context, dialogues : List<Dialogue>) :
    RecyclerView.Adapter<DialogueListAdapter.DialogueViewHodler>() {

    val dialogues : List<Dialogue>

    init {
        this.dialogues = dialogues
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogueViewHodler {
        val itemView = View.inflate(context, R.layout.dialogue_item, null)
        return DialogueViewHodler(itemView)
    }

    override fun getItemCount(): Int {
        return dialogues.size
    }

    override fun onBindViewHolder(holder: DialogueViewHodler, position: Int) {
        holder.BindDialogue(dialogues[position])
    }

    inner class DialogueViewHodler(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val view : View

        init {
            view = itemView
        }

        fun BindDialogue(dialogue : Dialogue) {

        }
    }
}