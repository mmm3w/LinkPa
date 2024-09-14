package com.mitsuki.linkpa.base.helper

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.mitsuki.linkpa.BuildConfig
import java.util.*

class DeviceName(context: Context) {

    private val sp: SharedPreferences by lazy {
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    }

    private var nameCache: String = ""

    private val defaultName by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Settings.Global.getString(context.contentResolver,
                Settings.Global.DEVICE_NAME)
        } else {
            UUID.randomUUID().toString()
        }
    }

    fun name(): String {
        return nameCache.ifEmpty {
            sp.getString("DeviceName", "") ?: ""
        }
            .ifEmpty {
                sp.edit().also {
                    it.putString("DeviceName", defaultName)
                    it.apply()
                }
                defaultName
            }.apply { nameCache = this }
    }

    fun set(name: String) {
        nameCache = name
        sp.edit().also {
            it.putString("DeviceName", nameCache)
            it.apply()
        }
    }
}