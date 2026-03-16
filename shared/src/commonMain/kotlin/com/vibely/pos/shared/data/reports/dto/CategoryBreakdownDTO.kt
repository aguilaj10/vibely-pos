package com.vibely.pos.shared.data.reports.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryBreakdownDTO(val categoryId: String, val categoryName: String, val revenue: Long, val transactionCount: Int)
