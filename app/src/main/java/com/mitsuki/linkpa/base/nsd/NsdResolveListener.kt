package com.mitsuki.linkpa.base.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

class NsdResolveListener : NsdManager.ResolveListener {
    override fun onServiceResolved(info: NsdServiceInfo) {

    }

    override fun onResolveFailed(info: NsdServiceInfo, p1: Int) {

    }
}