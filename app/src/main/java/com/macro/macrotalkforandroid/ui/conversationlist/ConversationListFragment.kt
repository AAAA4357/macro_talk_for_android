package com.macro.macrotalkforandroid.ui.conversationlist

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.databinding.FragmentConversationListBinding
import com.macro.macrotalkforandroid.ui.conversationlist.conversation.ConversationActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.Utils
import com.macro.macrotalkforandroid.ui.profilelist.AddProfileBindView
import com.macro.macrotalkforandroid.ui.profilelist.ProfileListAdapter

class ConversationListFragment : Fragment() {

    // 界面绑定对象
    private var _binding: FragmentConversationListBinding? = null
    private val binding get() = _binding!!

    // 会话列表适配器
    lateinit var conversationAdapter : ConversationListAdapter

    // 添加会话视图
    lateinit var addConversationView : AddConversationBindView

    // 创建视图
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 绑定布局
        _binding = FragmentConversationListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化会话适配器
        conversationAdapter = ConversationListAdapter(requireContext())

        // 初始化添加会话视图
        addConversationView = AddConversationBindView(resources, requireContext(), this@ConversationListFragment)

        return root
    }

    // 销毁视图
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 开始方法
    override fun onStart() {
        super.onStart()
        val list = binding.root.findViewById<RecyclerView>(R.id.conversation_list)
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val empty = binding.root.findViewById<TextView>(R.id.conversation_empty)
        if (conversationAdapter.conversationList.isEmpty()) {
            empty.visibility = View.VISIBLE
        }
        else {
            empty.visibility = View.INVISIBLE
        }
        // 设置点击监听器和长按监听器
        conversationAdapter.setOnItemClickListener(OnConversationItemClick())
        conversationAdapter.setOnItemLongClickListener(OnConversationItemLongClick())
        list.apply {
            this.adapter = conversationAdapter
            this.layoutManager = layoutManager
        }
        val addConversation = requireView().findViewById<FloatingActionButton>(R.id.conversation_addconversation)
        addConversation.setOnClickListener(AddConversationClick())
    }

    // 处理ActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val path = data.data!!.path!!.replace("/raw/", "")
            val bitmap = BitmapFactory.decodeFile(path)
            addConversationView.uploadImage(bitmap, path)
        }
    }

    // 添加会话
    fun addConversation(conversation : Conversation) {
        conversationAdapter.addItem(conversation)
        val empty = binding.root.findViewById<TextView>(R.id.conversation_empty)
        empty.visibility = View.INVISIBLE
    }

    // 删除会话
    fun removeConversation(index : Int) {
        conversationAdapter.removeItem(index)
        if (conversationAdapter.conversationList.size == 0) {
            val empty = binding.root.findViewById<TextView>(R.id.conversation_empty)
            empty.visibility = View.VISIBLE
        }
    }

    // 替换会话
    fun replaceConversation(index : Int, newConversation : Conversation) {
        conversationAdapter.replaceItem(index, newConversation)
    }

    // 添加会话点击监听器
    inner class AddConversationClick : View.OnClickListener {
        override fun onClick(v: View?) {
            addConversationView = AddConversationBindView(resources, requireContext(), this@ConversationListFragment)
            val dialog = CustomDialog.build()
            dialog.setCustomView(addConversationView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }
    }

    // 会话点击监听器
    inner class OnConversationItemClick : ConversationListAdapter.OnItemClickListener {
        override fun OnItemClick(view: View?, data: Conversation?) {
            ConversationActivity.conversation = data!!
            val intent = Intent(requireContext(), ConversationActivity()::class.java)
            startActivity(intent)
        }
    }

    // 会话长按监听器
    inner class OnConversationItemLongClick() : ConversationListAdapter.OnItemLongClickListener {
        var modifyConversationView = AddConversationBindView(resources, requireContext(), this@ConversationListFragment, true)

        override fun OnItemLongClick(v: View?, data: Conversation?) {
            PopMenu.show(v, listOf("删除", "修改"))
                .setOverlayBaseView(false)
                .setAlignGravity(Gravity.BOTTOM)
                .setWidth(Utils.dip2px(requireContext(), 80f))
                .setOnMenuItemClickListener { dialog, _, index ->
                    dialog.dismiss()
                    when (index) {
                        0 -> {
                            removeConversation(Utils.storageData.Conversations.indexOf(data!!))
                        }
                        1 -> {
                            modifyConversationView.index = Utils.storageData.Conversations.indexOf(data!!)
                            val dialog = CustomDialog.build()
                            dialog.setCustomView(modifyConversationView)
                            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
                            dialog.show()
                            modifyConversationView.loadConversation(data)
                        }
                        else -> {}
                    }
                    false
                }
        }
    }
}
