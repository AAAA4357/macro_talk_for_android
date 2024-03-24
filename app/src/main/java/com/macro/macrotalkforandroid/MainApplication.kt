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

        mainContext = applicationContext

        DialogX.init(mainContext)
        DialogX.globalStyle = MaterialStyle.style()


        x.Ext.init(this)
        x.Ext.setDebug(BuildConfig.DEBUG)

        Utils.load()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        XCrash.init(this)
    }

    companion object {
        lateinit var mainContext : Context
            private set
    }
}