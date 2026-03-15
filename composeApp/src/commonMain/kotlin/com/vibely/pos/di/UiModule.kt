package com.vibely.pos.di

import com.vibely.pos.ui.auth.LoginViewModel
import com.vibely.pos.ui.checkout.CheckoutViewModel
import com.vibely.pos.ui.customers.CustomersViewModel
import com.vibely.pos.ui.dashboard.DashboardViewModel
import com.vibely.pos.ui.inventory.InventoryViewModel
import com.vibely.pos.ui.sales.SalesListViewModel
import com.vibely.pos.ui.screens.categories.CategoriesViewModel
import com.vibely.pos.ui.suppliers.SuppliersViewModel
import org.koin.dsl.module

val uiModule = module {
    factory { LoginViewModel(get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    factory { CheckoutViewModel(get(), get(), get(), get(), get()) }
    factory { SalesListViewModel(get()) }
    factory { InventoryViewModel(get(), get()) }
    factory { CategoriesViewModel(get()) }
    factory { CustomersViewModel(get(), get()) }
    factory { SuppliersViewModel(get(), get()) }
}
