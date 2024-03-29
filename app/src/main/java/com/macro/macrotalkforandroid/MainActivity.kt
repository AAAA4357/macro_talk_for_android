package com.macro.macrotalkforandroid

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.macro.macrotalkforandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

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
                R.id.navigation_profile_list, R.id.navigation_conversation_list, R.id.navigation_ai_student, R.id.navigation_settings
            )
        )

        // 将顶部导航栏与导航控制器关联
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 将底部导航视图与导航控制器关联
        navView.setupWithNavController(navController)

        /*XXPermissions.with(this@MainActivity)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {

                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        Toast.makeText(this@MainActivity, "请手动授予权限防止软件崩溃", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "请授予权限防止软件崩溃", Toast.LENGTH_SHORT).show()
                    }
                }
            })*/

    }
}
