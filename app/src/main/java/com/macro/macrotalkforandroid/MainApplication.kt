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

        mainContext = applicationContext

        DialogX.init(mainContext)
        DialogX.globalStyle = MaterialStyle.style()


        x.Ext.init(this)
        x.Ext.setDebug(BuildConfig.DEBUG)

        XUI.init(this)
        XUI.initFontStyle("fonts/font_main.ttf");

        Utils.load()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base))
    }

    companion object {
        lateinit var mainContext : Context
            private set
    }
}