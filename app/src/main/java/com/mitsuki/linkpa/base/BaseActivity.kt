package com.mitsuki.linkpa.base

import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity


open class BaseActivity<VB : ViewBinding>(inflate: (LayoutInflater) -> VB) : ComponentActivity() {
    val binding by viewBinding(inflate)


    fun <T> CoroutineScope.launchCollect(flow: SharedFlow<T>, action: suspend (T) -> Unit) {
        launch { flow.collect { withContext(Dispatchers.Main) { action(it) } } }
    }


    fun lifeLaunch(state: Lifecycle.State, block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch { repeatOnLifecycle(state, block) }
    }


}