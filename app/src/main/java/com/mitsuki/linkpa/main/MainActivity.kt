package com.mitsuki.linkpa.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mitsuki.linkpa.base.BaseActivity
import com.mitsuki.linkpa.base.helper.DeviceName
import com.mitsuki.linkpa.base.nsd.NsdConnect
import com.mitsuki.linkpa.base.socket.SocketConnect
import com.mitsuki.linkpa.databinding.ActivityMainBinding
import kotlinx.coroutines.*


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val mViewModel by viewModels<MainViewModel>()
//    private val mAdapter by lazy { DeviceListAdapter(mViewModel.data) }
    private val deviceName by lazy { DeviceName(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.mainServerName.setText(deviceName.name())
        binding.mainStartSocket.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                SocketConnect.triggerServer(true)
            } else {
                SocketConnect.triggerServer(false)
            }
        }

        binding.mainStartServer.setOnClickListener { buttonView ->
            if (binding.mainDiscoveryMode.isChecked) {
                mViewModel.triggerService("", NsdConnect.NsdState.Discovery)
            } else {
                val name = binding.mainServerName.text.toString()
                if (name.isEmpty()) {
                    return@setOnClickListener
                }
                deviceName.set(name)
                mViewModel.triggerService(name, NsdConnect.NsdState.Discoverable)
            }
        }

//        mAdapter.onItemClick = {
//            startActivity(Intent(this, RemoteActivity::class.java).apply {
//                putExtra("ip", it.ip)
//                putExtra("port", it.port)
//            })
//        }

        binding.mainDeviceList.apply {
//            layoutManager = LinearLayoutManager(this@MainActivity)
//            adapter = mAdapter
        }



        lifeLaunch(Lifecycle.State.STARTED) {
            launchCollect(mViewModel.switchEnable) { binding.mainStartServer.isEnabled = it }
            launchCollect(mViewModel.serverMarkColor) {
                binding.mainServerStatus.setBackgroundColor(it)
            }
            launchCollect(mViewModel.discoveryModeCheck) {
                binding.mainDiscoveryMode.isEnabled = it.first
                binding.mainDiscoveryMode.isChecked = it.second
            }

            launchCollect(mViewModel.toastFlow) {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
            }

            launchCollect(mViewModel.serverPort) {
                binding.mainSocketServerStatus.setBackgroundColor(if (it > 0) Color.GREEN else Color.RED)
                binding.mainSocketPort.text = it.toString()
            }
            launchCollect(SocketConnect.clientEventFlow) { cmd ->
                if (cmd.first) {
                    cmd.second.action(this@MainActivity)
                }
            }
        }


    }
}