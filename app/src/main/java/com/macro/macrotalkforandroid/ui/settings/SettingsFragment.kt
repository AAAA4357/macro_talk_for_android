package com.macro.macrotalkforandroid.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.macro.macrotalkforandroid.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val char = root.findViewById<EditText>(R.id.setting_char)
        char.setText(Utils.SettingData.DefaultSplitChar)
        char.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                Utils.SettingData.DefaultSplitChar = s!!.toString()
                Utils.saveSetting()
            }
        })

        val collapse = root.findViewById<Switch>(R.id.setting_collapse)
        collapse.isChecked = Utils.SettingData.AutoCollaspe
        collapse.setOnClickListener {
            Utils.SettingData.AutoCollaspe = collapse.isChecked
            Utils.saveSetting()
        }

        val collapseCount = root.findViewById<EditText>(R.id.setting_collapse_count)
        collapseCount.setText(Utils.SettingData.AutoCollaspeCount.toString())
        collapse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                Utils.SettingData.AutoCollaspeCount = s!!.toString().toInt()
                Utils.saveSetting()
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}