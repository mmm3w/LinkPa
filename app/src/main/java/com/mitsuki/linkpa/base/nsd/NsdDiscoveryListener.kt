package com.mitsuki.linkpa.base.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class NsdDiscoveryListener : NsdManager.DiscoveryListener {

    private val mIsDiscovery: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isDiscoveryFlow: StateFlow<Boolean> get() = mIsDiscovery

    private val mDiscoveryEvent: MutableSharedFlow<NsdEvent> = MutableSharedFlow()
    val discoveryEventFlow: SharedFlow<NsdEvent> get() = mDiscoveryEvent


    override fun onDiscoveryStarted(serviceType: String) {
        Log.d("Nsd", "NsdDiscoveryListener -->onDiscoveryStarted")
        mIsDiscovery.update { true }
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryStarted(serviceType))
        }
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.d("Nsd", "NsdDiscoveryListener -->onStartDiscoveryFailed")
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryStartFailed(serviceType, errorCode))
        }
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.d("Nsd", "NsdDiscoveryListener -->onDiscoveryStopped")
        mIsDiscovery.update { false }
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryStopped(serviceType))
        }
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.d("Nsd", "NsdDiscoveryListener -->onStopDiscoveryFailed")
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryStopFailed(serviceType, errorCode))
        }
    }

    override fun onServiceFound(info: NsdServiceInfo) {
        Log.d("Nsd", "NsdDiscoveryListener -->onServiceFound")
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryServiceFound(info))
        }
    }

    override fun onServiceLost(info: NsdServiceInfo) {
        Log.d("Nsd", "NsdDiscoveryListener -->onServiceLost")
        GlobalScope.launch {
            mDiscoveryEvent.emit(NsdEvent.DiscoveryServiceLost(info))
        }
    }
}