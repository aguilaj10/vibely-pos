package com.vibely.pos.backend.di

import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test

class BackendModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `backendModule can be loaded into Koin without null included modules`() {
        koinApplication {
            modules(backendModule)
        }.close()
    }
}

