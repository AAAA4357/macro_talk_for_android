package com.macro.macrotalkforandroid

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.hailong.appupdate.AppUpdateManager
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.GuideDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.macro.macrotalkforandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val updateUrl = "http://macro.skyman.cloud:2100/api/Application/GetLatest/Android"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 设置布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取底部导航视图
        val navView: BottomNavigationView = binding.navView

        // 获取导航控制器
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 设置顶部导航栏的配置，包括导航目的地的 ID
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile_list, R.id.navigation_conversation_list, R.id.navigation_online, R.id.navigation_settings
            )
        )

        // 将顶部导航栏与导航控制器关联
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 将底部导航视图与导航控制器关联
        navView.setupWithNavController(navController)

        if (!Utils.SettingData.HintDisplyed) {
            Utils.ShowHint(0)
        }
    }
}
