package com.mitsuki.linkpa.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    /* toast */
    private val mToastFlow: MutableSharedFlow<String> by lazy { MutableSharedFlow() }
    val toastFlow: SharedFlow<String> get() = mToastFlow

    protected fun toast(content: String) {
        viewModelScope.launch { mToastFlow.emit(content) }
    }

    fun <T> SharedFlow<T>.launchCollect(action: suspend (T) -> Unit) {
        viewModelScope.launch { collect { action(it) } }
    }
}