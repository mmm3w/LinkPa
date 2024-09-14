package com.mitsuki.linkpa.remote

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mitsuki.linkpa.base.BaseViewModel
import com.mitsuki.linkpa.base.socket.Command
import com.mitsuki.linkpa.base.socket.SocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : BaseViewModel(application) {
    private val mClient: SocketClient by lazy { SocketClient() }

    private val mClientStatusColor: MutableStateFlow<Int> = MutableStateFlow(Color.RED)
    val clientStatusColor: StateFlow<Int> get() = mClientStatusColor

    init {
        mClient.clientConnectStatusFlow.launchCollect {
            if (it) {
                mClientStatusColor.update { Color.GREEN }
            } else {
                mClientStatusColor.update { Color.RED }
            }
        }

        mClient.sendStatusFlow.launchCollect {
            Log.d("Socket", "Command:${it.first} progress:${it.second}")
        }


    }

    fun sendCommand(cmd: Command) {
        viewModelScope.launch { mClient.sendSuspend(cmd) }
    }

    fun connect(host: String, port: Int) {
        mClient.connect(host, port)
    }

    override fun onCleared() {
        super.onCleared()
        mClient.disconnect()
    }
}