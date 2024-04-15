package com.macro.macrotalkforandroid

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MaterialStyle
import com.xuexiang.xui.XUI
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.xutils.BuildConfig
import org.xutils.x




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

        XUI.init(this)
        XUI.initFontStyle("fonts/font_main.ttf");

        // 加载数据
        Utils.load()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base))
    }

    companion object {
        // 应用上下文
        lateinit var mainContext: Context
            private set
    }
}
