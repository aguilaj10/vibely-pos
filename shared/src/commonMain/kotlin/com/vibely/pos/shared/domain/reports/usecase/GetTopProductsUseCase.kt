package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Use case for retrieving top-performing products by revenue for a specified time period.
 *
 * Returns a ranked list of products with detailed performance metrics including
 * quantity sold, revenue, cost, and profit. Validates date range parameters and
 * ensures data integrity before querying the repository.
 *
 * **Business Rules:**
 * - When [ReportPeriod.CUSTOM] is used, both start and end dates must be provided
 * - Start date must be before or equal to end date
 * - Dates must not be in the future
 * - Results are ordered by revenue descending
 * - Only completed transactions are included
 *
 * **Use Cases:**
 * - Identify best-selling products
 * - Inventory restocking prioritization
 * - Product profitability analysis
 * - Sales performance dashboards
 *
 * @property repository The reports repository for data access.
 *
 * @see ProductPerformance
 * @see ReportsRepository.getTopProducts
 */
class GetTopProductsUseCase(private val repository: ReportsRepository) {
    /**
     * Retrieves top-performing products for the specified time period.
     *
     * Validates input parameters according to business rules before querying
     * the repository. Returns products ranked by total revenue in descending order.
     *
     * @param period The time period for the report (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for CUSTOM period (inclusive).
     *                        Required when [period] is [ReportPeriod.CUSTOM].
     * @param customEndDate Optional end date for CUSTOM period (inclusive).
     *                      Required when [period] is [ReportPeriod.CUSTOM].
     * @return [Result.Success] containing a list of [ProductPerformance] ordered by revenue,
     *         or [Result.Error] with validation failure or repository error message.
     *         Returns empty list if no sales occurred in the period.
     *
     * **Validation Errors:**
     * - "Custom period requires both start and end dates" - When CUSTOM period lacks dates
     * - "Start date must be before or equal to end date" - When date range is invalid
     * - "Dates cannot be in the future" - When dates exceed current time
     */
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>> {
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

        return repository.getTopProducts(period, customStartDate, customEndDate)
    }
}
