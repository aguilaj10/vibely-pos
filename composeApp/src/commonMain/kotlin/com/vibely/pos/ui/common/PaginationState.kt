package com.vibely.pos.ui.common

/**
 * Pagination state for managing paginated data in ViewModels.
 *
 * @property currentPage Current page number (1-indexed)
 * @property pageSize Number of items per page
 * @property hasMore Whether there are more items available (from server response)
 */
data class PaginationState(val currentPage: Int = 1, val pageSize: Int = 50, val hasMore: Boolean = false) {
    /**
     * Whether there is a previous page available.
     */
    val hasPreviousPage: Boolean
        get() = currentPage > 1

    /**
     * Whether there is a next page available.
     */
    val hasNextPage: Boolean
        get() = hasMore

    /**
     * Creates a new state for the next page.
     */
    fun nextPage(): PaginationState = if (hasMore) copy(currentPage = currentPage + 1) else this

    /**
     * Creates a new state for the previous page.
     */
    fun previousPage(): PaginationState = if (currentPage > 1) copy(currentPage = currentPage - 1) else this

    /**
     * Creates a new state with updated hasMore flag (from server response).
     */
    fun withHasMore(more: Boolean): PaginationState = copy(hasMore = more)

    /**
     * Resets pagination to the first page.
     */
    fun reset(): PaginationState = copy(currentPage = 1, hasMore = false)
}
