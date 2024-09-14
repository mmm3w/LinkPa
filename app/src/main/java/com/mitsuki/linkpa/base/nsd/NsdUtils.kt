package com.mitsuki.linkpa.base.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.core.content.ContextCompat

object NsdUtils {

    fun registerService(
        context: Context,
        name: String,
        port: Int,
        listener: NsdManager.RegistrationListener,
    ): Boolean {
        return try {
            ContextCompat.getSystemService(context, NsdManager::class.java)?.run {
                registerService(NsdServiceInfo().also {
                    it.serviceName = name
                    it.port = port
                    it.serviceType = "_http._tcp."
                }, NsdManager.PROTOCOL_DNS_SD, listener)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun unregisterService(context: Context, listener: NsdManager.RegistrationListener): Boolean {
        return try {
            ContextCompat.getSystemService(context, NsdManager::class.java)?.run {
                unregisterService(listener)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun discoverServices(context: Context, listener: NsdManager.DiscoveryListener): Boolean {
        return try {
            ContextCompat.getSystemService(context, NsdManager::class.java)?.run {
                discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, listener)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopServiceDiscovery(context: Context, listener: NsdManager.DiscoveryListener): Boolean {
        return try {
            ContextCompat.getSystemService(context, NsdManager::class.java)?.run {
                stopServiceDiscovery(listener)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resolveService(
        context: Context,
        info: NsdServiceInfo,
        listener: NsdManager.ResolveListener,
    ): Boolean {
        return try {
            ContextCompat.getSystemService(context, NsdManager::class.java)?.run {
                resolveService(info, listener)
                true } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}