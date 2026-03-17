package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Use case for retrieving sales breakdown by product category for a specified time period.
 *
 * Returns category-level sales analytics showing which product categories are
 * driving revenue and customer engagement. Validates date range parameters
 * before querying the repository.
 *
 * **Business Rules:**
 * - When [ReportPeriod.CUSTOM] is used, both start and end dates must be provided
 * - Start date must be before or equal to end date
 * - Dates must not be in the future
 * - Results are typically ordered by revenue descending
 * - Only completed transactions are included
 *
 * **Use Cases:**
 * - Category performance comparison
 * - Revenue mix analysis
 * - Merchandising optimization
 * - Inventory allocation by category
 *
 * @property repository The reports repository for data access.
 *
 * @see CategoryBreakdown
 * @see ReportsRepository.getCategoryBreakdown
 */
class GetCategoryBreakdownUseCase(private val repository: ReportsRepository) {
    /**
     * Retrieves category breakdown for the specified time period.
     *
     * Validates input parameters according to business rules before querying
     * the repository. Returns categories with their sales metrics.
     *
     * @param period The time period for the report (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for CUSTOM period (inclusive).
     *                        Required when [period] is [ReportPeriod.CUSTOM].
     * @param customEndDate Optional end date for CUSTOM period (inclusive).
     *                      Required when [period] is [ReportPeriod.CUSTOM].
     * @return [Result.Success] containing a list of [CategoryBreakdown] ordered by revenue,
     *         or [Result.Error] with validation failure or repository error message.
     *         Returns empty list if no sales occurred in the period.
     *
     * **Validation Errors:**
     * - "Custom period requires both start and end dates" - When CUSTOM period lacks dates
     * - "Start date must be before or equal to end date" - When date range is invalid
     * - "Dates cannot be in the future" - When dates exceed current time
     */
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CategoryBreakdown>> {
        // Validate custom period dates
        if (period == ReportPeriod.CUSTOM) {
            if (customStartDate == null || customEndDate == null) {
                return Result.Error(
                    message = "Custom period requires both start and end dates",
                    code = "INVALID_DATE_RANGE",
                )
            }

            if (customStartDate > customEndDate) {
                return Result.Error(
                    message = "Start date must be before or equal to end date",
                    code = "INVALID_DATE_RANGE",
                )
            }

            val now = Clock.System.now()
            if (customStartDate > now || customEndDate > now) {
                return Result.Error(
                    message = "Dates cannot be in the future",
                    code = "INVALID_DATE_RANGE",
                )
            }
        }

        return repository.getCategoryBreakdown(period, customStartDate, customEndDate)
    }
}
