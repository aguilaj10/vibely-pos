package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Use case for retrieving a comprehensive sales report for a specified time period.
 *
 * Orchestrates the retrieval of aggregated sales metrics including revenue, costs,
 * profit, transaction count, and average transaction value. Validates date range
 * parameters before delegating to the repository layer.
 *
 * **Business Rules:**
 * - When [ReportPeriod.CUSTOM] is used, both start and end dates must be provided
 * - Start date must be before or equal to end date
 * - Dates must not be in the future
 * - Only completed transactions are included in calculations
 *
 * **Use Cases:**
 * - Dashboard summary metrics display
 * - Period-over-period performance comparison
 * - Financial reporting and analysis
 * - Business intelligence dashboards
 *
 * @property repository The reports repository for data access.
 *
 * @see SalesReport
 * @see ReportsRepository.getSalesReport
 */
class GetSalesReportUseCase(private val repository: ReportsRepository) {
    /**
     * Retrieves a sales report for the specified time period.
     *
     * Validates input parameters according to business rules before querying
     * the repository. For predefined periods (TODAY, THIS_WEEK, THIS_MONTH),
     * the custom dates are ignored. For CUSTOM period, both dates are required.
     *
     * @param period The time period for the report (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for CUSTOM period (inclusive).
     *                        Required when [period] is [ReportPeriod.CUSTOM].
     * @param customEndDate Optional end date for CUSTOM period (inclusive).
     *                      Required when [period] is [ReportPeriod.CUSTOM].
     * @return [Result.Success] containing the [SalesReport] with aggregated metrics,
     *         or [Result.Error] with validation failure or repository error message.
     *
     * **Validation Errors:**
     * - "Custom period requires both start and end dates" - When CUSTOM period lacks dates
     * - "Start date must be before or equal to end date" - When date range is invalid
     * - "Dates cannot be in the future" - When dates exceed current time
     */
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport> {
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

        return repository.getSalesReport(period, customStartDate, customEndDate)
    }
}
