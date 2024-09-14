package com.mitsuki.linkpa.main

import android.app.Application
import android.graphics.Color
import com.mitsuki.linkpa.base.BaseViewModel
import com.mitsuki.linkpa.base.nsd.NsdConnect
import com.mitsuki.linkpa.base.nsd.NsdEvent
import com.mitsuki.linkpa.base.socket.SocketConnect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : BaseViewModel(application) {

//    val data by lazy { NotifyQueueData(Device.DIFF) }

    private val mSwitchEnable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val switchEnable: StateFlow<Boolean> get() = mSwitchEnable

    private val mServerMarkColor: MutableStateFlow<Int> = MutableStateFlow(Color.RED)
    val serverMarkColor: StateFlow<Int> get() = mServerMarkColor

    private val mDiscoveryModeCheck: MutableStateFlow<Pair<Boolean, Boolean>> =
        MutableStateFlow(true to false)
    val discoveryModeCheck: StateFlow<Pair<Boolean, Boolean>> get() = mDiscoveryModeCheck

    private val mServerPort: MutableStateFlow<Int> = MutableStateFlow(0)
    val serverPort: StateFlow<Int> get() = mServerPort





    private var waiting = false

    init {
        NsdConnect.nsdRegisterEventFlow.launchCollect { eventHandle(it) }
        NsdConnect.nsdDiscoveryEventFlow.launchCollect { eventHandle(it) }
        NsdConnect.nsdStatusFlow.launchCollect {
            when (it) {
                NsdConnect.NsdState.None -> {
                    mServerMarkColor.update { Color.RED }
                    mDiscoveryModeCheck.update { true to mDiscoveryModeCheck.value.second }

                }
                NsdConnect.NsdState.Discoverable -> {
                    mServerMarkColor.update { Color.GREEN }
                    mDiscoveryModeCheck.update { false to false }
                }
                NsdConnect.NsdState.Discovery -> {
                    mServerMarkColor.update { Color.YELLOW }
                    mDiscoveryModeCheck.update { false to true }
                }
            }
        }

        SocketConnect.serverStatusFlow.launchCollect {
            if (it) {
                mServerPort.update { SocketConnect.socketPort }
            } else {
                mServerPort.update { 0 }
            }
        }
    }

    fun triggerService(name: String, mode: NsdConnect.NsdState) {
        if (waiting) {
            toast("too frequent")
            return
        }
        waiting = true
        if (NsdConnect.nsdStatus == NsdConnect.NsdState.None) {
            if (mode == NsdConnect.NsdState.Discoverable) {
                NsdConnect.nsdName = name
                NsdConnect.nsdPort = mServerPort.value
            }
            NsdConnect.setMode(getApplication(), mode)
        } else {
            NsdConnect.setMode(getApplication(), NsdConnect.NsdState.None)
        }
    }

    private suspend fun eventHandle(event: NsdEvent) {
        when (event) {
            is NsdEvent.DiscoverableRegistered -> withContext(Dispatchers.Main) {
                toast("DiscoverableRegistered")
            }
            is NsdEvent.DiscoverableRegistrationFailed -> withContext(Dispatchers.Main) {
                toast("DiscoverableRegistrationFailed")
            }

            is NsdEvent.DiscoverableUnregistered -> withContext(Dispatchers.Main) {
                toast("DiscoverableUnregistered")
            }
            is NsdEvent.DiscoverableUnregistrationFailed -> withContext(Dispatchers.Main) {
                toast("DiscoverableUnregistrationFailed")
            }

            is NsdEvent.DiscoveryStarted -> withContext(Dispatchers.Main) {
                toast("DiscoveryStarted")
//                data.postUpdate(NotifyData.Clear())
            }
            is NsdEvent.DiscoveryStartFailed -> withContext(Dispatchers.Main) { toast("DiscoveryStartFailed") }

            is NsdEvent.DiscoveryStopped -> withContext(Dispatchers.Main) {
                toast("DiscoveryStopped")
            }
            is NsdEvent.DiscoveryStopFailed -> withContext(Dispatchers.Main) { toast("DiscoveryStopFailed") }
            is NsdEvent.DiscoveryServiceFound -> {
                val result = NsdConnect.resolveService(getApplication(), event.serviceInfo)
                if (result.first < 0) {
//                    data.postUpdate(NotifyData.Insert(
//                        Device(result.second.serviceName,
//                        result.second.host?.hostAddress,
//                        result.second.port)
//                    ))
                }
            }
            is NsdEvent.DiscoveryServiceLost -> {
//                data.postUpdate(NotifyData.Remove(
//                    Device(event.serviceInfo.serviceName,
//                    event.serviceInfo.host?.hostAddress,
//                    event.serviceInfo.port)
//                ))
            }
        }
        waiting = false
        mSwitchEnable.update { true }
    }


}