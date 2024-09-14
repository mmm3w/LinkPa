package com.mitsuki.linkpa.base

import android.app.Application
import com.mitsuki.linkpa.base.socket.Command

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Command.init(this)
    }
}