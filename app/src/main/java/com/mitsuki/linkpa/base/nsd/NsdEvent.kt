package com.mitsuki.linkpa.base.nsd

import android.net.nsd.NsdServiceInfo

sealed class NsdEvent {
    class DiscoverableRegistered(val serviceInfo: NsdServiceInfo) : NsdEvent()
    class DiscoverableRegistrationFailed(val serviceInfo: NsdServiceInfo, val errorCode: Int) :
        NsdEvent()

    class DiscoverableUnregistered(val serviceInfo: NsdServiceInfo) : NsdEvent()
    class DiscoverableUnregistrationFailed(val serviceInfo: NsdServiceInfo, val errorCode: Int) :
        NsdEvent()


    class DiscoveryStarted(val serviceType: String) : NsdEvent()
    class DiscoveryStartFailed(val serviceType: String, val errorCode: Int) : NsdEvent()

    class DiscoveryStopped(val serviceType: String) : NsdEvent()
    class DiscoveryStopFailed(val serviceType: String, val errorCode: Int) : NsdEvent()
    
    class DiscoveryServiceFound(val serviceInfo: NsdServiceInfo) : NsdEvent()
    class DiscoveryServiceLost(val serviceInfo: NsdServiceInfo) : NsdEvent()
}
