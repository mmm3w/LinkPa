package com.mitsuki.linkpa.base.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.mitsuki.linkpa.base.nsd.NsdEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
@OptIn(DelicateCoroutinesApi::class)
class NsdRegistrationListener : NsdManager.RegistrationListener {

    private val mIsRegistered: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRegisteredFlow: StateFlow<Boolean> get() = mIsRegistered
    val isRegistered: Boolean get() = mIsRegistered.value

    private val mRegisterEvent: MutableSharedFlow<NsdEvent> = MutableSharedFlow()
    val registerEventFlow: SharedFlow<NsdEvent> get() = mRegisterEvent

    override fun onServiceRegistered(info: NsdServiceInfo) {
        Log.d("Nsd", "NsdRegistrationListener -->onServiceRegistered  $info")
        mIsRegistered.update { true }
        GlobalScope.launch {
            mRegisterEvent.emit(NsdEvent.DiscoverableRegistered(info))
        }
    }

    override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
        Log.d("Nsd", "NsdRegistrationListener -->onRegistrationFailed $info $errorCode")
        GlobalScope.launch {
            mRegisterEvent.emit(NsdEvent.DiscoverableRegistrationFailed(info, errorCode))
        }
    }

    override fun onServiceUnregistered(info: NsdServiceInfo) {
        Log.d("Nsd", "NsdRegistrationListener -->onServiceUnregistered")
        mIsRegistered.update { false }
        GlobalScope.launch {
            mRegisterEvent.emit(NsdEvent.DiscoverableUnregistered(info))
        }
    }

    override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
        Log.d("Nsd", "NsdRegistrationListener -->onUnregistrationFailed")
        GlobalScope.launch {
            mRegisterEvent.emit(NsdEvent.DiscoverableUnregistrationFailed(info, errorCode))
        }
    }
}