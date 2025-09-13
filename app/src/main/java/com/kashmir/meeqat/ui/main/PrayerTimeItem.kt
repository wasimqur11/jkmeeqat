package com.kashmir.meeqat.ui.main

data class PrayerTimeItem(
    val name: String,
    val time: String,
    val iconRes: Int,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)