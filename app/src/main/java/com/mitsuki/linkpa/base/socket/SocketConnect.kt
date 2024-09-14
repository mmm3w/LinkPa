package com.mitsuki.linkpa.base.socket

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

@OptIn(DelicateCoroutinesApi::class)
object SocketConnect {
    private val mSocketServerStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val serverStatusFlow: StateFlow<Boolean> get() = mSocketServerStatus
    val isServerStarted get() = mSocketServerStatus.value

    private val mClientCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val clientCountFlow: StateFlow<Int> get() = mClientCount
    val clientCount get() = mClientCount.value

    private val mClientEvent: MutableSharedFlow<Pair<Boolean, Command>> = MutableSharedFlow()
    val clientEventFlow: SharedFlow<Pair<Boolean, Command>> get() = mClientEvent

    private var serverSocket: ServerSocket? = null
    val socketPort: Int get() = serverSocket?.localPort ?: 0

    private val clientMap: MutableMap<String, ClientLink> = hashMapOf()

    fun triggerServer(enable: Boolean) {
        if (enable == isServerStarted) {
            return
        }

        if (enable) {
            GlobalScope.launch(Dispatchers.IO) {
                serverSocket = ServerSocket(0)
                Log.d("Socket", "Socket start port:${serverSocket?.localPort}")
                mSocketServerStatus.update { true }
                try {
                    while (serverSocket?.isClosed != true) {
                        serverSocket?.accept()?.also { socket ->
                            Log.d("Socket", "Client is connected, ${socket.inetAddress}")
                            socket.inetAddress.hostAddress.ifEmpty { null }?.also { ip ->
                                //kill old connect
                                clientMap.remove(ip)?.also { client ->
                                    if (client.isConnected) {
                                        client.disconnect()
                                    }
                                }
                                //create new connect
                                clientMap[ip] = ClientLink(ip, socket, mClientEvent, clientMap)
                                //update client count
                                mClientCount.update { clientMap.size }
                            }
                        }
                    }
                } catch (e: SocketException) {
                    Log.d("Socket", "finish. $e")
                    mSocketServerStatus.update { false }
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
//                clientArray.clear()
//                mClientCount.update { clientArray.size }
                try {
                    serverSocket?.close()
                } catch (ignore: Exception) {
                } finally {
                    serverSocket = null
                }
            }
        }
    }


    private class ClientLink(
        private val id: String,
        private val socket: Socket,
        private val event: MutableSharedFlow<Pair<Boolean, Command>>,
        private val clientMap: MutableMap<String, ClientLink>,
    ) {

        val isConnected get() = socket.isConnected && !socket.isClosed

        init {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    socket.getInputStream()
                        .run { DataInputStream(this) }
                        .use { inputStream ->
                            while (true) {
                                val mark = inputStream.readInt()
                                Command.matchCommand(mark)?.also { command ->
                                    event.emit(false to command)
                                    command.read(inputStream)
                                    event.emit(true to command)
                                }
                            }
                        }
                } catch (ignore: Exception) {
                } finally {
                    clientMap.remove(id)
                    disconnect()
                }
            }
        }

        fun disconnect() {
            if (!socket.isClosed) {
                try {
                    socket.close()
                } catch (ignore: Exception) {
                }
            }
        }
    }

}