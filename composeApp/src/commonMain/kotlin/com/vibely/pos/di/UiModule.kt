package com.vibely.pos.di

import com.vibely.pos.ui.auth.LoginViewModel
import com.vibely.pos.ui.checkout.CheckoutViewModel
import com.vibely.pos.ui.common.ConnectivityViewModel
import com.vibely.pos.ui.customers.CustomersViewModel
import com.vibely.pos.ui.dashboard.DashboardViewModel
import com.vibely.pos.ui.inventory.InventoryViewModel
import com.vibely.pos.ui.purchaseorders.PurchaseOrdersViewModel
import com.vibely.pos.ui.reports.ReportsViewModel
import com.vibely.pos.ui.sales.SalesListViewModel
import com.vibely.pos.ui.screens.categories.CategoriesViewModel
import com.vibely.pos.ui.settings.SettingsViewModel
import com.vibely.pos.ui.shifts.ShiftsViewModel
import com.vibely.pos.ui.suppliers.SuppliersViewModel
import com.vibely.pos.ui.users.UsersViewModel
import org.koin.dsl.module

val uiModule =
    module {
        factory { ConnectivityViewModel() }
        factory { LoginViewModel(get()) }
        factory { DashboardViewModel(get(), get(), get()) }
        factory { CheckoutViewModel(get(), get(), get(), get(), get(), get(), get()) }
        factory { SalesListViewModel(get(), get(), get()) }
        factory { InventoryViewModel(get(), get(), get(), get(), get(), get(), get()) }
        factory { CategoriesViewModel(get(), get(), get()) }
        factory { CustomersViewModel(get(), get(), get(), get(), get()) }
        factory { SuppliersViewModel(get(), get(), get(), get(), get()) }
        factory { PurchaseOrdersViewModel(get(), get(), get(), get(), get(), get(), get()) }
        factory { ShiftsViewModel(get(), get(), get()) }
        factory { UsersViewModel(get(), get(), get(), get(), get()) }
        factory { ReportsViewModel(get(), get(), get(), get(), get()) }
        factory { SettingsViewModel(get(), get(), get(), get(), get()) }
    }
