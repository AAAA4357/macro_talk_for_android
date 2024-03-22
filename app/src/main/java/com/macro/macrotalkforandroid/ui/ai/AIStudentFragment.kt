package com.macro.macrotalkforandroid.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alibaba.dashscope.exception.ApiException
import com.alibaba.dashscope.exception.NoApiKeyException
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.databinding.FragmentAiStudentBinding

class AIStudentFragment : Fragment() {

    private var _binding: FragmentAiStudentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiStudentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        root.findViewById<Button>(R.id.TestButton).setOnClickListener(OnTestClick())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OnTestClick() : OnClickListener {
        override fun onClick(v: View?) {
            AIHelper.Init()
            try {
                val result = AIHelper.quickStart()
                binding.root.findViewById<TextView>(R.id.TestOutput).text = result
            }
            catch (_ : NoApiKeyException) {
                Toast.makeText(requireContext(), "你忘记加api了", Toast.LENGTH_SHORT).show()
            }
        }
    }
}