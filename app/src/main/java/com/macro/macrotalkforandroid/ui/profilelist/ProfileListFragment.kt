package com.macro.macrotalkforandroid.ui.profilelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.databinding.FragmentProfileListBinding

// 学生资料列表 Fragment
class ProfileListFragment : Fragment() {

    private var _binding: FragmentProfileListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager

    // 两个学生资料标签页
    var ProfileTabs : List<ProfileTab> = listOf(ProfileTab.newInstance(true), ProfileTab.newInstance(false))

    // 标签页标题
    var tabTitles : Array<String> = listOf("预制档案", "自定义档案").toTypedArray()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化视图
        tabLayout = root.findViewById(R.id.profile_tab_layout)
        viewPager = root.findViewById(R.id.profile_view_page)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置ViewPager适配器
        val adapter = ProfilePagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ViewPager适配器
    inner class ProfilePagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return ProfileTabs[position]
        }

        override fun getCount(): Int {
            return ProfileTabs.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }
}
