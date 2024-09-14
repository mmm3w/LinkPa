package com.mitsuki.linkpa.base

import android.app.Activity
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

fun ViewGroup.createItemView(@LayoutRes layout: Int): View =
    LayoutInflater.from(context).inflate(layout, this, false)


fun <VB : ViewBinding> Activity.viewBinding(inflate: (LayoutInflater) -> VB) = lazy {
    inflate(layoutInflater).apply { setContentView(root) }
}

fun <VB : ViewBinding> RecyclerView.ViewHolder.viewBinding(bind: (View) -> VB) =
    lazy { bind(itemView) }

fun Cursor?.getStringWithDefault(tag: String, dv: String = ""): String {
    if (this == null) return dv
    return getColumnIndex(tag).let {
        if (it >= 0) getStringOrNull(it) ?: dv else dv
    }
}

fun Cursor?.getIntWithDefault(tag: String, dv: Int = 0): Int {
    if (this == null) return dv
    return getColumnIndex(tag).let {
        if (it >= 0) getIntOrNull(it) ?: dv else dv
    }
}

fun Cursor?.getLongWithDefault(tag: String, dv: Long = 0L): Long {
    if (this == null) return dv
    return getColumnIndex(tag).let {
        if (it >= 0) getLongOrNull(it) ?: dv else dv
    }
}