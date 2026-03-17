package com.vibely.pos.di

import android.content.Context
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<Context> { context.applicationContext }
}
