package com.Macro.macrotalkforandroid.ui.conversationlist

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Macro.macrotalkforandroid.AddConversationBindView
import com.Macro.macrotalkforandroid.Conversation
import com.Macro.macrotalkforandroid.ConversationListAdapter
import com.Macro.macrotalkforandroid.R
import com.Macro.macrotalkforandroid.Utils
import com.Macro.macrotalkforandroid.databinding.FragmentConversationListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kongzue.dialogx.dialogs.CustomDialog
import java.io.File

class ConversationListFragment : Fragment() {

    private var _binding: FragmentConversationListBinding? = null

    private val binding get() = _binding!!

    lateinit var conversationList : MutableList<Conversation>

    lateinit var conversationAdapter : ConversationListAdapter

    lateinit var addConversationView : AddConversationBindView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        conversationList = Utils.storageData.Conversations

        conversationAdapter = ConversationListAdapter(requireContext(), conversationList)

        addConversationView = AddConversationBindView(resources, requireContext(), this@ConversationListFragment)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val list = binding.root.findViewById<RecyclerView>(R.id.conversation_list)
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val empty = binding.root.findViewById<TextView>(R.id.conversation_empty)
        if (conversationList.isEmpty()) {
            empty.visibility = View.VISIBLE
        }
        else {
            empty.visibility = View.INVISIBLE
        }
        list.apply {
            this.adapter = adapter
            this.layoutManager = layoutManager
        }
        val addConversation = requireView().findViewById<FloatingActionButton>(R.id.conversation_addconversation)
        addConversation.setOnClickListener(AddConversationClick())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val path = data.data!!.path!!.replace("/raw/", "")
            val bitmap = BitmapFactory.decodeFile(path)
            addConversationView.uploadImage(bitmap, path)
        }
    }

    fun addConversation(conversation : Conversation) {
        conversationList.add(conversation)
        conversationAdapter.addItem(conversation)
        val empty = binding.root.findViewById<TextView>(R.id.profile_empty)
        empty.visibility = View.INVISIBLE
    }

    fun removeConversation(index : Int) {
        conversationList.removeAt(index)
        conversationAdapter.removeItem(index)
        if (conversationList.size == 0) {
            val empty = binding.root.findViewById<TextView>(R.id.profile_empty)
            empty.visibility = View.VISIBLE
        }
    }

    fun replaceConversation(index : Int, newConversation : Conversation) {
        conversationList[index] = newConversation
        conversationAdapter.replaceItem(index, newConversation)
    }

    inner class AddConversationClick : View.OnClickListener {
        override fun onClick(v: View?) {
            addConversationView = AddConversationBindView(resources, requireContext(), this@ConversationListFragment)
            val dialog = CustomDialog.build()
            dialog.setCustomView(addConversationView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }
    }
}