package com.mitsuki.linkpa.base.socket

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.charset.StandardCharsets


sealed class Command {
    companion object {
        private const val COMMAND_TEST = 0
        private const val COMMAND_TOAST = 1

        private const val COMMAND_APK_INSTALL_PERMISSION_CHECK = 101
        private const val COMMAND_APK_SHARE = 102

        private lateinit var filesDir: String
        private lateinit var cacheDir: String

        fun init(context: Context) {
            filesDir = context.filesDir.absolutePath
            cacheDir = context.cacheDir.absolutePath
        }

        fun matchCommand(mark: Int): Command? {
            return when (mark) {
                COMMAND_TEST -> Unknown
                COMMAND_TOAST -> CToast()
                COMMAND_APK_SHARE -> ApkShare()
                else -> null
            }
        }
    }

    abstract suspend fun action(context: Context)

    protected val mReadStatus: MutableStateFlow<Int> = MutableStateFlow(0)
    val readStatusFlow: StateFlow<Int> get() = mReadStatus

    abstract suspend fun read(inputStream: DataInputStream)

    abstract suspend fun write(
        outputStream: DataOutputStream,
        emitter: MutableStateFlow<Pair<Int, Int>>,
    )

    override fun toString(): String {
        return "$javaClass"
    }

    fun writeText(text: String, outputStream: DataOutputStream) {
        val data = text.toByteArray(StandardCharsets.UTF_8)
        val length: Int = data.size
        outputStream.writeInt(length)
        outputStream.write(data)
    }

    fun readText(inputStream: DataInputStream): String {
        val textLength = inputStream.readInt()
        val data = ByteArray(textLength)
        inputStream.readFully(data)
        return String(data, StandardCharsets.UTF_8)
    }

    object Unknown : Command() {
        override suspend fun action(context: Context) {
            /* do nothing */
        }

        override suspend fun read(inputStream: DataInputStream) {
            /* do nothing */
        }

        override suspend fun write(
            outputStream: DataOutputStream,
            emitter: MutableStateFlow<Pair<Int, Int>>,
        ) {
            /* do nothing */
        }
    }


    class CToast(var text: String = "") : Command() {
        override suspend fun action(context: Context) {
            withContext(Dispatchers.IO) { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
        }

        override suspend fun read(inputStream: DataInputStream) {
            withContext(Dispatchers.IO) {
                mReadStatus.update { 0 }
                text = readText(inputStream)
                mReadStatus.update { 100 }
            }
        }

        override suspend fun write(
            outputStream: DataOutputStream,
            emitter: MutableStateFlow<Pair<Int, Int>>,
        ) {
            if (text.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    emitter.update { COMMAND_TOAST to -1 }
                    outputStream.writeInt(COMMAND_TOAST)
                    writeText(text, outputStream)
                    emitter.update { -1 to -1 }
                }
            }
        }
    }

    class ApkShare(
        var fileName: String = "",
        var safeDir: String = "",
        var inputStream: InputStream? = null,
    ) : Command() {
        override suspend fun action(context: Context) {
            val dir = File(filesDir, safeDir)
            withContext(Dispatchers.Main) {
                Toast.makeText(context,
                    "file-> ${File(dir, fileName).absolutePath}",
                    Toast.LENGTH_SHORT).show()
            }
        }

        override suspend fun read(inputStream: DataInputStream) {
            withContext(Dispatchers.IO) {
                mReadStatus.update { 0 }
                fileName = readText(inputStream)
                safeDir = readText(inputStream)
                val dir = File(filesDir, safeDir)
                dir.mkdirs()
                File(dir, fileName).outputStream().use { outputStream ->
                    val length = inputStream.readInt()
                    val buffer = ByteArray(1024)

                    var bytesCopied: Int = 0
                    var surplus: Int = if (length <= buffer.size) length else buffer.size
                    var bytes = inputStream.read(buffer, 0, surplus)
                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        mReadStatus.update { 100 * bytesCopied / length }
                        if (bytesCopied == length) {
                            break
                        }
                        surplus = length - bytesCopied
                        if (surplus >= buffer.size) {
                            surplus = buffer.size
                        }
                        bytes = inputStream.read(buffer, 0, surplus)
                    }
                }
            }
        }

        override suspend fun write(
            outputStream: DataOutputStream,
            emitter: MutableStateFlow<Pair<Int, Int>>,
        ) {
            inputStream?.use { fileInput ->
                withContext(Dispatchers.IO) {
                    emitter.update { COMMAND_APK_SHARE to 0 }
                    outputStream.writeInt(COMMAND_APK_SHARE)
                    writeText(fileName, outputStream)
                    writeText(safeDir, outputStream)
                    val length = fileInput.available()
                    outputStream.writeInt(length)
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(1024)
                    var bytes = fileInput.read(buffer)
                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        bytes = fileInput.read(buffer)
                        emitter.update { COMMAND_APK_SHARE to (100 * bytesCopied / length).toInt() }
                    }
                    emitter.update { -1 to -1 }
                }
            }
        }
    }
}