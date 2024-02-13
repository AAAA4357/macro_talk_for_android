package com.example.macrotalkforandroid

import android.app.Application
import android.content.Context
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MaterialStyle

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        mainContext = applicationContext

        DialogX.init(mainContext)

        DialogX.globalStyle = MaterialStyle.style()
    }

    companion object {
        lateinit var mainContext : Context
            private set
    }
}