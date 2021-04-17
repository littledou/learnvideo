package cn.idu.kotlinnativetest

import android.app.Application
import android.content.Context

class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        applicationContext
    }

    companion object {
        var app: Context? = null
            private set
    }
}