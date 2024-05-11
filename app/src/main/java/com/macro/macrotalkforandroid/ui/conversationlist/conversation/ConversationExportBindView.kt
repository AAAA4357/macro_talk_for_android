package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import kotlin.math.exp


class ConversationExportBindView(val context: Context, val resources: Resources, val dialogues: List<Dialogue>, val dialogueAdapter: DialogueListAdapter)
    : OnBindView<CustomDialog>(R.layout.fragment_export) {
    lateinit var view: View

    var fullCut : Boolean? = null

    var cutWay : List<Int> = listOf()

    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        val radios = view.findViewById<RadioGroup>(R.id.cutmode)
        radios.setOnCheckedChangeListener(OnCutModeChanged())
        val cancel = view.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = view.findViewById<TextView>(R.id.confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog))
        val list = view.findViewById<RecyclerView>(R.id.dialogue_preview)
        list.setBackgroundColor(Color.parseColor(Utils.SettingData.ConversationBgColor))
    }

    // 剪切模式变化监听器
    inner class OnCutModeChanged : OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            val list = view.findViewById<RecyclerView>(R.id.dialogue_preview)
            val text = view.findViewById<TextView>(R.id.cut_hint)
            if (checkedId % 2 != 0) {
                list.visibility = View.INVISIBLE
                text.visibility = View.INVISIBLE
                fullCut = true
            } else {
                list.visibility = View.VISIBLE
                text.visibility = View.VISIBLE
                val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                dialogueAdapter.setOnItemClickListener(OnDialogueClick())
                dialogueAdapter.setOnItemLongClickListener(null)
                list.apply {
                    this.adapter = dialogueAdapter
                    this.layoutManager = layoutManager
                }
                fullCut = false
            }
            val confirm = view.findViewById<TextView>(R.id.confirm)
            confirm.isEnabled = true
        }
    }

    // 取消按钮点击监听器
    inner class OnCancelClick(val dialog: CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    // 确定按钮点击监听器
    inner class OnConfirmClick(val dialog: CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            if (fullCut == null) {
                PopTip.show("请选择切割模式")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
                return
            }
            if (fullCut == false && cutWay.isEmpty()) {
                PopTip.show("请选择至少一条对话")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
                return
            }
            val list = view.findViewById<RecyclerView>(R.id.dialogue_preview)
            val left = view.findViewById<TextView>(R.id.export_marginLeft)
            val top = view.findViewById<TextView>(R.id.export_marginTop)
            val right = view.findViewById<TextView>(R.id.export_marginRight)
            val bottom = view.findViewById<TextView>(R.id.export_marginBottom)
            list.setPadding(left.text.toString().toInt(), top.text.toString().toInt(), right.text.toString().toInt(), bottom.text.toString().toInt())
            val animator = list.itemAnimator
            list.itemAnimator = null
            val exportPreviewBindView = ConversationPreviewBindView(context)
            exportPreviewBindView.bitmaps = listOf()
            for (dialogue in dialogueAdapter.dialogues.toMutableList()) {
                dialogueAdapter.removeDialogue(0)
            }
            val printDialogues = dialogues.toMutableList()
            if (fullCut == true) {
                for (dialogue in dialogues) {
                    dialogueAdapter.addDialogue(dialogue)
                }
                exportPreviewBindView.bitmaps += Utils.shotRecyclerView(list)!!
            } else {
                for (count in cutWay) {
                    for (i in 0..count) {
                        dialogueAdapter.addDialogue(printDialogues[0])
                        printDialogues.removeAt(0)
                    }
                    exportPreviewBindView.bitmaps += Utils.shotRecyclerView(list)!!
                    for (dialogue in dialogueAdapter.dialogues.toMutableList()) {
                        dialogueAdapter.removeDialogue(0)
                    }
                }
            }
            exportPreviewBindView.upperDialog = dialog
            for (dialogue in dialogueAdapter.dialogues.toMutableList()) {
                dialogueAdapter.removeDialogue(0)
            }
            for (dialogue in dialogues) {
                dialogueAdapter.addDialogue(dialogue)
            }
            list.itemAnimator = animator
            val dialog = CustomDialog.build()
            dialog.setCustomView(exportPreviewBindView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }
    }

    var cutAll : Boolean = false

    inner class OnDialogueClick() : DialogueListAdapter.OnItemClickListener {
        override fun OnItemClick(view: View?, data: Dialogue?) {
            if (cutAll) {
                cutWay = listOf()
                cutAll = false
            }
            val index = dialogueAdapter.dialogues.indexOf(data!!)
            cutWay += index
            for (i in 0..index) {
                dialogueAdapter.removeDialogue(0)
            }
            if (dialogueAdapter.itemCount == 0) {
                cutAll = true
                for (dialogue in dialogues) {
                    dialogueAdapter.addDialogue(dialogue)
                }
            }
        }
    }
}
