package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RadioGroup.LayoutParams
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils


class ConversationExportBindView(val context : Context, val resources : Resources, val dialogues : List<Dialogue>, val adapter: DialogueListAdapter)
    : OnBindView<CustomDialog>(R.layout.fragment_export) {
    lateinit var view: View

    var bitmaps = listOf<Bitmap>()

    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        val radios = view.findViewById<RadioGroup>(R.id.cutmode)
        radios.setOnCheckedChangeListener(OnCutModeChanged())
        val cancel = view.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = view.findViewById<TextView>(R.id.confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog))
    }

    inner class OnCutModeChanged : OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                4 -> {

                }
                5 -> {
                    val list = view.findViewById<RecyclerView>(R.id.dialogue_preview)
                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    val adapter = BitmapListAdapter(context, bitmaps)
                    list.apply {
                        this.adapter = adapter
                        this.layoutManager = layoutManager
                    }
                }
                else -> return
            }
            val confirm = view.findViewById<TextView>(R.id.confirm)
            confirm.isEnabled = true
        }
    }

    inner class OnCancelClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    inner class OnConfirmClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    inner class BitmapListAdapter(val context: Context, val bitmaps : List<Bitmap>) : RecyclerView.Adapter<BitmapListAdapter.BitmapViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmapViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.conversation_export_item, null, false)
            return BitmapViewHolder(view)
        }

        override fun getItemCount(): Int {
            return bitmaps.size
        }

        override fun onBindViewHolder(holder: BitmapViewHolder, position: Int) {
            val image = holder.view.findViewById<ImageView>(R.id.export_preview)
            image.setImageBitmap(bitmaps[position])
        }

        inner class BitmapViewHolder(val view : View) : ViewHolder(view)
    }
}