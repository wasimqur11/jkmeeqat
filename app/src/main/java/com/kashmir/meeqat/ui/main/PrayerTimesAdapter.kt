package com.kashmir.meeqat.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kashmir.meeqat.R
import com.kashmir.meeqat.databinding.ItemPrayerTimeBinding

data class PrayerTimeItem(
    val name: String,
    val time: String,
    val iconRes: Int,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val hasNotification: Boolean = true
)

class PrayerTimesAdapter : ListAdapter<PrayerTimeItem, PrayerTimesAdapter.PrayerTimeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerTimeViewHolder {
        val binding = ItemPrayerTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrayerTimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrayerTimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PrayerTimeViewHolder(private val binding: ItemPrayerTimeBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerTimeItem) {
            binding.apply {
                tvPrayerName.text = item.name
                tvPrayerTime.text = item.time
                ivPrayerIcon.setImageResource(item.iconRes)
                
                // Update colors based on prayer status
                when {
                    item.isNext -> {
                        tvPrayerName.setTextColor(root.context.getColor(R.color.prayer_next))
                        tvPrayerTime.setTextColor(root.context.getColor(R.color.prayer_next))
                        root.strokeColor = root.context.getColor(R.color.prayer_next)
                        root.strokeWidth = 2
                    }
                    item.isPassed -> {
                        tvPrayerName.setTextColor(root.context.getColor(R.color.prayer_passed))
                        tvPrayerTime.setTextColor(root.context.getColor(R.color.prayer_passed))
                        root.strokeColor = root.context.getColor(R.color.prayer_passed)
                        root.strokeWidth = 1
                    }
                    else -> {
                        tvPrayerName.setTextColor(root.context.getColor(R.color.text_primary_light))
                        tvPrayerTime.setTextColor(root.context.getColor(R.color.text_secondary_light))
                        root.strokeColor = root.context.getColor(R.color.primary_light)
                        root.strokeWidth = 1
                    }
                }
                
                // Show/hide notification icon
                ivNotificationIcon.visibility = if (item.hasNotification) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PrayerTimeItem>() {
        override fun areItemsTheSame(oldItem: PrayerTimeItem, newItem: PrayerTimeItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: PrayerTimeItem, newItem: PrayerTimeItem): Boolean {
            return oldItem == newItem
        }
    }
}