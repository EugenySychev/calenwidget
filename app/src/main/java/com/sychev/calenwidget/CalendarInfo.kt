package com.sychev.calenwidget

import kotlinx.serialization.Serializable

@Serializable
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val color: Int
)
