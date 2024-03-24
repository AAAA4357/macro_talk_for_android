package com.macro.macrotalkforandroid

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MaterialStyle
import org.xutils.BuildConfig
import org.xutils.x
import xcrash.XCrash




class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化应用上下文
        mainContext = applicationContext

        // 初始化 DialogX 库
        DialogX.init(mainContext)
        DialogX.globalStyle = MaterialStyle.style()

        // 初始化 xUtils 库
        x.Ext.init(this)
        x.Ext.setDebug(BuildConfig.DEBUG)

        // 加载数据
        Utils.load()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        XCrash.init(this)
    }

    companion object {
        // 应用上下文
        lateinit var mainContext: Context
            private set
    }
}
