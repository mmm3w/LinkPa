package com.mitsuki.linkpa.main

import androidx.recyclerview.widget.DiffUtil

data class Device(
    val name: String,
    val ip: String?,
    val port: Int,
) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Device && other.name == this.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + ip.hashCode()
        result = 31 * result + port
        return result
    }
}