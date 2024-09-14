package com.mitsuki.linkpa.base.socket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

@OptIn(DelicateCoroutinesApi::class)
class SocketClient : Closeable {

    private var mSocket: Socket? = null
    private var mOutputStream: DataOutputStream? = null

    private val mClientConnectStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val clientConnectStatusFlow: StateFlow<Boolean> get() = mClientConnectStatus
    val isClientConnected get() = mClientConnectStatus.value


    private val mSendStatus: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(-1 to -1)
    val sendStatusFlow: StateFlow<Pair<Int, Int>> get() = mSendStatus


    private val mSendLock by lazy { Mutex() }


    fun connect(host: String, port: Int) {
        if (isClientConnected) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                mSocket = Socket()
                mClientConnectStatus.update { true }
                mSocket?.connect(InetSocketAddress(host, port), 5000)
                mOutputStream = mSocket?.getOutputStream()?.run { DataOutputStream(this) }
            } catch (e: Exception) {
                disconnect()
            }
        }
    }

    fun send(command: Command) {
        GlobalScope.launch {
            sendSuspend(command)
        }
    }

    suspend fun sendSuspend(command: Command) {
        mSendLock.withLock {
            withContext(Dispatchers.IO) {
                try {
                    mOutputStream?.also { command.write(it, mSendStatus) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mSendStatus.update { -1 to -1 }
                    disconnect()
                }
            }
        }
    }

    fun disconnect() {
//        if (!(mSocket != null && mSocket?.isConnected == true && mSocket?.isClosed == false)) {
        try {
            mOutputStream?.close()
            mOutputStream = null
            mSocket?.close()
            mSocket = null
        } catch (ignore: Exception) {
        } finally {
            mClientConnectStatus.update { false }
        }
//        }
    }

    override fun close() {
        disconnect()
    }
}