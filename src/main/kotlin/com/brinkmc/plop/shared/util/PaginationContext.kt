package com.brinkmc.plop.shared.util

data class PaginationContext<T>(
    val items: List<T>,
    val rowRange: IntRange, // This defines if we use 3 rows (0..2) or 5 rows (0..4)
    private var _currentPage: Int = 0
) {
    // Calculate how many items fit based on the rows provided
    val pageSize: Int
        get() = (rowRange.last - rowRange.first + 1) * 9

    val currentPage: Int
        get() = _currentPage

    fun nextPage() {
        _currentPage++
    }

    fun prevPage() {
        _currentPage--
    }
}