package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Use case for retrieving customer analytics and purchasing behavior metrics.
 *
 * Returns customer spending patterns, visit frequency, and engagement data
 * to support CRM, loyalty programs, and targeted marketing. Includes both
 * registered customers and walk-in customers. Validates date range parameters
 * before querying the repository.
 *
 * **Business Rules:**
 * - When [ReportPeriod.CUSTOM] is used, both start and end dates must be provided
 * - Start date must be before or equal to end date
 * - Dates must not be in the future
 * - Results are typically ordered by total spent descending
 * - Only completed transactions are included
 *
 * **Use Cases:**
 * - Top customer identification (VIP customers)
 * - Customer lifetime value analysis
 * - Customer segmentation
 * - Loyalty program optimization
 * - Churn risk detection
 *
 * @property repository The reports repository for data access.
 *
 * @see CustomerAnalytics
 * @see ReportsRepository.getCustomerAnalytics
 */
class GetCustomerAnalyticsUseCase(private val repository: ReportsRepository) {
    /**
     * Retrieves customer analytics for the specified time period.
     *
     * Validates input parameters according to business rules before querying
     * the repository. Returns customers ranked by total spending.
     *
     * @param period The time period for the report (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for CUSTOM period (inclusive).
     *                        Required when [period] is [ReportPeriod.CUSTOM].
     * @param customEndDate Optional end date for CUSTOM period (inclusive).
     *                      Required when [period] is [ReportPeriod.CUSTOM].
     * @return [Result.Success] containing a list of [CustomerAnalytics] ordered by total spent,
     *         or [Result.Error] with validation failure or repository error message.
     *         Returns empty list if no sales occurred in the period.
     *
     * **Validation Errors:**
     * - "Custom period requires both start and end dates" - When CUSTOM period lacks dates
     * - "Start date must be before or equal to end date" - When date range is invalid
     * - "Dates cannot be in the future" - When dates exceed current time
     */
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CustomerAnalytics>> {
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

        return repository.getCustomerAnalytics(period, customStartDate, customEndDate)
    }
}
