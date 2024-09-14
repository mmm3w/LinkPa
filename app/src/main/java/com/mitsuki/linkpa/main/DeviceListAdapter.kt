//package com.mitsuki.lynkcopa.main
//
//import android.annotation.SuppressLint
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.mitsuki.armory.adapter.notify.NotifyQueueData
//import com.mitsuki.linkpa.main.Device
//import com.mitsuki.lynkcopa.R
//import com.mitsuki.lynkcopa.base.createItemView
//import com.mitsuki.lynkcopa.base.viewBinding
//import com.mitsuki.lynkcopa.databinding.ItemDevicesBinding
//
//class DeviceListAdapter(private val mData: NotifyQueueData<Device>) :
//    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
//    init {
//        mData.attachAdapter(this)
//    }
//
//    var onItemClick: ((Device) -> Unit)? = null
//
//    private val mItemClick = { view: View ->
//        val viewHolder = view.tag as ViewHolder
//        onItemClick?.invoke(mData.item(viewHolder.bindingAdapterPosition))
//        Unit
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(parent).apply {
//            itemView.tag = this
//            itemView.setOnClickListener(mItemClick)
//        }
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(mData.item(position))
//    }
//
//    override fun getItemCount(): Int {
//        return mData.count
//    }
//
//
//    class ViewHolder(parent: ViewGroup) :
//        RecyclerView.ViewHolder(parent.createItemView(R.layout.item_devices)) {
//        private val binding by viewBinding(ItemDevicesBinding::bind)
//
//        val textView = binding.deviceName
//
//        @SuppressLint("SetTextI18n")
//        fun bind(data: Device) {
//            textView.text = "${data.name}  ${data.ip}:${data.port}"
//        }
//
//    }
//
//
//}