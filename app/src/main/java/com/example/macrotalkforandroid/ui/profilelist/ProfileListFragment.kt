package com.example.macrotalkforandroid.ui.profilelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.macrotalkforandroid.ProfileTab
import com.example.macrotalkforandroid.R
import com.example.macrotalkforandroid.databinding.FragmentProfileListBinding
import com.google.android.material.tabs.TabLayout


class ProfileListFragment : Fragment() {

    private var _binding: FragmentProfileListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var tabLayout : TabLayout

    private lateinit var viewPager : ViewPager

    var ProfileTabs : List<ProfileTab> = listOf(ProfileTab.newInstance(true), ProfileTab.newInstance(false))

    var tabTitles : Array<String> = listOf("预制档案", "自定义档案").toTypedArray()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tabLayout = root.findViewById(R.id.profile_tab_layout)
        viewPager = root.findViewById(R.id.profile_view_page)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ProfilePagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    inner class ProfilePagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return ProfileTabs[position]
        }

        override fun getCount(): Int {
            return ProfileTabs.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return super.instantiateItem(container, position)
        }

        //返回tablayout的标题文字;
        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }


}