package com.vibely.pos.shared.data.reports.dto

import kotlinx.serialization.Serializable

@Serializable
data class SalesTrendDTO(val timestamp: Long, val revenue: Long, val transactionCount: Int)
