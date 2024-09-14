package com.mitsuki.linkpa.remote

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import com.mitsuki.linkpa.base.BaseActivity
import com.mitsuki.linkpa.base.socket.Command
import com.mitsuki.linkpa.databinding.ActivityRemoteBinding
import java.io.File

class RemoteActivity : BaseActivity<ActivityRemoteBinding>(ActivityRemoteBinding::inflate) {

    private val mViewModel: RemoteViewModel by viewModels()

    private val apkPick by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.also { uri ->
                DocumentFile.fromSingleUri(this@RemoteActivity, uri)?.also { documentFile ->
                    contentResolver.openInputStream(uri)?.also { inputStream ->

                    }

                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val host: String = intent.getStringExtra("ip") ?: kotlin.run {
            finish()
            ""
        }

        val port = intent.getIntExtra("port", 0)

        mViewModel.connect(host, port)

        lifeLaunch(Lifecycle.State.STARTED) {
            launchCollect(mViewModel.clientStatusColor) {
                binding.remoteConnectStatus.setBackgroundColor(it)
            }
        }

        binding.remoteTest.setOnClickListener {
            mViewModel.sendCommand(Command.CToast("this is test toast."))
        }

        binding.remoteSendSelf.setOnClickListener {
            mViewModel.sendCommand(Command.ApkShare("self.apk",
                "/apk",
                File(applicationInfo.sourceDir).inputStream()))
        }
    }

}