package com.vibely.pos.ui.common

/**
 * Result of a paginated data fetch operation.
 *
 * @property items The list of items for the current page
 * @property hasMore Whether there are more items available on the next page
 */
data class PaginatedResult<T>(val items: List<T>, val hasMore: Boolean) {
    companion object {
        /**
         * Creates a PaginatedResult from a list of items and expected page size.
         * If items.size == pageSize, assumes there might be more pages.
         */
        fun <T> from(items: List<T>, pageSize: Int): PaginatedResult<T> = PaginatedResult(
            items = items,
            hasMore = items.size >= pageSize,
        )
    }
}
