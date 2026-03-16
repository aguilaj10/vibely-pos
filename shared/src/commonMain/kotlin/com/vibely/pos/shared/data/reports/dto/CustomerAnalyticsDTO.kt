package com.vibely.pos.shared.data.reports.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomerAnalyticsDTO(val customerId: String?, val customerName: String, val totalSpent: Long, val visitCount: Int, val lastVisit: Long)
