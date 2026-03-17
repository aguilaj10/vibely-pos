package com.vibely.pos.shared.di

import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.auth.usecase.LoginUseCase
import com.vibely.pos.shared.domain.auth.usecase.LogoutUseCase
import com.vibely.pos.shared.domain.auth.usecase.RefreshTokenUseCase
import com.vibely.pos.shared.domain.currency.usecase.CreateExchangeRateUseCase
import com.vibely.pos.shared.domain.currency.usecase.DeleteExchangeRateUseCase
import com.vibely.pos.shared.domain.currency.usecase.GetActiveCurrenciesUseCase
import com.vibely.pos.shared.domain.currency.usecase.GetAllExchangeRatesUseCase
import com.vibely.pos.shared.domain.currency.usecase.UpdateExchangeRateUseCase
import com.vibely.pos.shared.domain.customer.usecase.AddLoyaltyPointsUseCase
import com.vibely.pos.shared.domain.customer.usecase.CreateCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.DeleteCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.GetAllCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.GetCustomerPurchaseHistoryUseCase
import com.vibely.pos.shared.domain.customer.usecase.SearchCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.UpdateCustomerUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetDashboardSummaryUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetLowStockProductsUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetRecentTransactionsUseCase
import com.vibely.pos.shared.domain.inventory.usecase.AdjustStockUseCase
import com.vibely.pos.shared.domain.inventory.usecase.CreateCategoryUseCase
import com.vibely.pos.shared.domain.inventory.usecase.CreateProductUseCase
import com.vibely.pos.shared.domain.inventory.usecase.DeleteProductUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetAllProductsUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetCategoriesUseCase
import com.vibely.pos.shared.domain.inventory.usecase.UpdateCategoryUseCase
import com.vibely.pos.shared.domain.inventory.usecase.UpdateProductUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.CreatePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.DeletePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.GetAllPurchaseOrdersUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.GetPurchaseOrderByIdUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.ReceivePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.UpdatePurchaseOrderStatusUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.UpdatePurchaseOrderUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetCategoryBreakdownUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetCustomerAnalyticsUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetSalesReportUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetSalesTrendUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetTopProductsUseCase
import com.vibely.pos.shared.domain.sales.usecase.AddToCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.CompleteSaleUseCase
import com.vibely.pos.shared.domain.sales.usecase.GetSalesUseCase
import com.vibely.pos.shared.domain.sales.usecase.RemoveFromCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import com.vibely.pos.shared.domain.sales.usecase.UpdateCartUseCase
import com.vibely.pos.shared.domain.shift.usecase.CloseShiftUseCase
import com.vibely.pos.shared.domain.shift.usecase.GetCurrentShiftUseCase
import com.vibely.pos.shared.domain.shift.usecase.GetShiftHistoryUseCase
import com.vibely.pos.shared.domain.shift.usecase.GetShiftSummaryUseCase
import com.vibely.pos.shared.domain.shift.usecase.OpenShiftUseCase
import com.vibely.pos.shared.domain.supplier.usecase.CreateSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.DeleteSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.GetAllSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.SearchSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.UpdateSupplierUseCase
import com.vibely.pos.shared.domain.user.usecase.AssignRoleUseCase
import com.vibely.pos.shared.domain.user.usecase.ChangePasswordUseCase
import com.vibely.pos.shared.domain.user.usecase.CreateUserUseCase
import com.vibely.pos.shared.domain.user.usecase.DeleteUserUseCase
import com.vibely.pos.shared.domain.user.usecase.GetAllUsersUseCase
import com.vibely.pos.shared.domain.user.usecase.GetUserByIdUseCase
import com.vibely.pos.shared.domain.user.usecase.SearchUsersUseCase
import com.vibely.pos.shared.domain.user.usecase.UpdateUserStatusUseCase
import com.vibely.pos.shared.domain.user.usecase.UpdateUserUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for the Domain layer.
 *
 * Registers use cases and repository interfaces.
 * Repository interfaces are defined here but their implementations
 * are provided by [dataModule].
 *
 * As use cases and repository interfaces are created, register them here:
 * ```
 * val domainModule = module {
 *     singleOf(::GetProductsUseCase)
 *     singleOf(::CreateOrderUseCase)
 * }
 * ```
 */
val domainModule =
    module {
        // Auth use cases
        singleOf(::LoginUseCase)
        singleOf(::LogoutUseCase)
        singleOf(::GetCurrentUserUseCase)
        singleOf(::RefreshTokenUseCase)

        // Dashboard use cases
        singleOf(::GetDashboardSummaryUseCase)
        singleOf(::GetRecentTransactionsUseCase)
        singleOf(::GetLowStockProductsUseCase)

        // Sales use cases
        singleOf(::SearchProductsUseCase)
        singleOf(::AddToCartUseCase)
        singleOf(::RemoveFromCartUseCase)
        singleOf(::CompleteSaleUseCase)
        singleOf(::GetSalesUseCase)
        singleOf(::UpdateCartUseCase)

        // Inventory use cases
        singleOf(::GetAllProductsUseCase)
        singleOf(::GetCategoriesUseCase)
        singleOf(::CreateProductUseCase)
        singleOf(::UpdateProductUseCase)
        singleOf(::DeleteProductUseCase)
        singleOf(::CreateCategoryUseCase)
        singleOf(::UpdateCategoryUseCase)
        singleOf(::AdjustStockUseCase)

        // Customer use cases
        singleOf(::GetAllCustomersUseCase)
        singleOf(::CreateCustomerUseCase)
        singleOf(::UpdateCustomerUseCase)
        singleOf(::DeleteCustomerUseCase)
        singleOf(::SearchCustomersUseCase)
        singleOf(::GetCustomerPurchaseHistoryUseCase)
        singleOf(::AddLoyaltyPointsUseCase)

        // Supplier use cases
        singleOf(::GetAllSuppliersUseCase)
        singleOf(::CreateSupplierUseCase)
        singleOf(::UpdateSupplierUseCase)
        singleOf(::DeleteSupplierUseCase)
        singleOf(::SearchSuppliersUseCase)

        // PurchaseOrder use cases
        singleOf(::GetAllPurchaseOrdersUseCase)
        singleOf(::GetPurchaseOrderByIdUseCase)
        singleOf(::CreatePurchaseOrderUseCase)
        singleOf(::UpdatePurchaseOrderUseCase)
        singleOf(::UpdatePurchaseOrderStatusUseCase)
        singleOf(::DeletePurchaseOrderUseCase)
        singleOf(::ReceivePurchaseOrderUseCase)

        // Shift use cases
        singleOf(::OpenShiftUseCase)
        singleOf(::CloseShiftUseCase)
        singleOf(::GetCurrentShiftUseCase)
        singleOf(::GetShiftHistoryUseCase)
        singleOf(::GetShiftSummaryUseCase)

        // User management use cases
        singleOf(::GetAllUsersUseCase)
        singleOf(::GetUserByIdUseCase)
        singleOf(::CreateUserUseCase)
        singleOf(::UpdateUserUseCase)
        singleOf(::UpdateUserStatusUseCase)
        singleOf(::AssignRoleUseCase)
        singleOf(::ChangePasswordUseCase)
        singleOf(::DeleteUserUseCase)
        singleOf(::SearchUsersUseCase)

        // Reports use cases
        singleOf(::GetSalesReportUseCase)
        singleOf(::GetSalesTrendUseCase)
        singleOf(::GetCategoryBreakdownUseCase)
        singleOf(::GetTopProductsUseCase)
        singleOf(::GetCustomerAnalyticsUseCase)

        // Currency use cases
        singleOf(::GetAllExchangeRatesUseCase)
        singleOf(::CreateExchangeRateUseCase)
        singleOf(::UpdateExchangeRateUseCase)
        singleOf(::DeleteExchangeRateUseCase)
        singleOf(::GetActiveCurrenciesUseCase)
    }
