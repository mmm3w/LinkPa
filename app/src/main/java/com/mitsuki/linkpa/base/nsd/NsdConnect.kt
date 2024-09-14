package com.mitsuki.linkpa.base.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(DelicateCoroutinesApi::class)
object NsdConnect {

    private val mNsdStatus: MutableStateFlow<NsdState> = MutableStateFlow(NsdState.None)
    private val mServiceDiscovery: MutableSharedFlow<Pair<Boolean, NsdServiceInfo>> =
        MutableSharedFlow()


    private val mNsdRegistrationListener: NsdRegistrationListener = NsdRegistrationListener()
    private val mNsdDiscoveryListener: NsdDiscoveryListener = NsdDiscoveryListener()

    var nsdName: String? = null
    var nsdPort: Int = 0

    val nsdStatus: NsdState get() = mNsdStatus.value
    val nsdStatusFlow: StateFlow<NsdState> get() = mNsdStatus
    val serviceDiscoveryFlow: SharedFlow<Pair<Boolean, NsdServiceInfo>>
        get() = mServiceDiscovery

    val nsdRegisterEventFlow get() = mNsdRegistrationListener.registerEventFlow
    val nsdDiscoveryEventFlow get() = mNsdDiscoveryListener.discoveryEventFlow

    init {
        GlobalScope.launch {
            mNsdRegistrationListener.isRegisteredFlow.collect {
                if (it) {
                    mNsdStatus.update { NsdState.Discoverable }
                } else {
                    mNsdStatus.update { NsdState.None }
                }
            }
        }

        GlobalScope.launch {
            mNsdDiscoveryListener.isDiscoveryFlow.collect {
                if (it) {
                    mNsdStatus.update { NsdState.Discovery }
                } else {
                    mNsdStatus.update { NsdState.None }
                }
            }
        }
    }

    fun setMode(context: Context, mode: NsdState): Boolean {
        if (mode == mNsdStatus.value) {
            return false
        }

        if (mode == NsdState.None) {
            return when (mNsdStatus.value) {
                NsdState.Discovery -> NsdUtils.stopServiceDiscovery(context, mNsdDiscoveryListener)
                NsdState.Discoverable -> NsdUtils.unregisterService(
                    context,
                    mNsdRegistrationListener
                )
                else -> false
            }
        } else {
            if (mNsdStatus.value == NsdState.None) {
                if (mode == NsdState.Discovery) {
                    return NsdUtils.discoverServices(context, mNsdDiscoveryListener)
                } else if (mode == NsdState.Discoverable) {
                    val name = nsdName ?: ""
                    return if (name.isEmpty() || nsdPort !in 1..65535) {
                        false
                    } else {
                        NsdUtils.registerService(context, name, nsdPort, mNsdRegistrationListener)
                    }
                }
            }
            return false
        }
    }


    suspend fun resolveService(context: Context, info: NsdServiceInfo): Pair<Int, NsdServiceInfo> {
        return suspendCoroutine { continuation ->
            val result = NsdUtils.resolveService(
                context,
                info,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                        continuation.resume(errorCode to info)
                    }

                    override fun onServiceResolved(info: NsdServiceInfo) {
                        continuation.resume(-1 to info)
                    }
                },
            )
            if (!result) {
                continuation.resume(0 to info)
            }
        }
    }


    enum class NsdState {
        None, Discovery, Discoverable
    }
}