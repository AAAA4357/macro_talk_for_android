package com.macro.macrotalkforandroid

import android.app.Application
import android.content.Context
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MaterialStyle
import org.xutils.BuildConfig
import org.xutils.x

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        mainContext = applicationContext

        DialogX.init(mainContext)
        DialogX.globalStyle = MaterialStyle.style()


        x.Ext.init(this)
        x.Ext.setDebug(BuildConfig.DEBUG);
    }

    companion object {
        lateinit var mainContext : Context
            private set
    }
}