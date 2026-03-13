package com.vibely.pos.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin dependency injection with the shared module hierarchy.
 *
 * This function sets up the core DI modules following Clean Architecture layers:
 * - [domainModule] — Use cases and repository interfaces
 * - [dataModule] — Repository implementations and data sources
 * - [presentationModule] — ViewModels
 *
 * Platform-specific modules and additional configuration can be provided
 * via [platformModules] and [appDeclaration].
 *
 * ## Usage
 *
 * ### Android (in Application class)
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         initKoin(
 *             platformModules = listOf(androidModule),
 *             appDeclaration = { androidContext(this@MyApplication) }
 *         )
 *     }
 * }
 * ```
 *
 * ### Desktop / JS / WasmJS
 * ```kotlin
 * fun main() {
 *     initKoin()
 *     // ... start app
 * }
 * ```
 *
 * ### iOS (from Swift)
 * ```swift
 * KoinInitializerKt.doInitKoin(platformModules: [iosModule], appDeclaration: nil)
 * ```
 *
 * @param platformModules Additional platform-specific Koin modules.
 * @param appDeclaration Optional Koin application configuration (e.g., androidContext, logging).
 * @return The configured [KoinApplication] instance.
 */
fun initKoin(platformModules: List<Module> = emptyList(), appDeclaration: KoinAppDeclaration? = null): KoinApplication = startKoin {
    appDeclaration?.invoke(this)
    modules(sharedModules() + platformModules)
}

/**
 * Returns the list of shared Koin modules, ordered by Clean Architecture layer.
 *
 * Useful when integrating with frameworks that manage their own Koin lifecycle
 * (e.g., Ktor's `install(Koin)` or Compose's `KoinApplication`).
 */
fun sharedModules(): List<Module> = listOf(
    domainModule,
    dataModule,
    presentationModule,
)
