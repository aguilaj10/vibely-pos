package com.vibely.pos.backend.di

import com.vibely.pos.backend.config.SupabaseConfig
import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.backend.services.CategoryService
import com.vibely.pos.backend.services.CustomerService
import com.vibely.pos.backend.services.DashboardService
import com.vibely.pos.backend.services.InventoryService
import com.vibely.pos.backend.services.ProductService
import com.vibely.pos.backend.services.SaleService
import com.vibely.pos.backend.services.PurchaseOrderService
import com.vibely.pos.backend.services.ShiftService
import com.vibely.pos.backend.services.SupplierService
import com.vibely.pos.backend.services.TokenService
import com.vibely.pos.backend.services.UserManagementService
import com.vibely.pos.backend.services.UserRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

@Suppress("UndocumentedPublicProperty")
val backendModule =
    module {
        single<SupabaseClient> { SupabaseConfig.client }

        single { UserRepository(get()) }

        single { TokenService(get()) }

        single { AuthService(get(), get()) }

        single { DashboardService(get()) }

        single { ProductService(get()) }

    single { SaleService(get()) }

    single { CategoryService(get()) }

    single { InventoryService(get()) }

    single { CustomerService(get()) }

    single { SupplierService(get()) }

    single { PurchaseOrderService(get()) }

    single { ShiftService(get()) }

    single { UserManagementService(get(), get()) }
    }
