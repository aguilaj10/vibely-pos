package com.vibely.pos.backend.data.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vibely.pos.backend.data.room.dao.CategoryDao
import com.vibely.pos.backend.data.room.dao.CustomerDao
import com.vibely.pos.backend.data.room.dao.PaymentDao
import com.vibely.pos.backend.data.room.dao.ProductDao
import com.vibely.pos.backend.data.room.dao.SaleDao
import com.vibely.pos.backend.data.room.entity.CategoryEntity
import com.vibely.pos.backend.data.room.entity.CustomerEntity
import com.vibely.pos.backend.data.room.entity.PaymentEntity
import com.vibely.pos.backend.data.room.entity.ProductEntity
import com.vibely.pos.backend.data.room.entity.SaleEntity
import com.vibely.pos.backend.data.room.entity.SaleItemEntity
import kotlinx.coroutines.Dispatchers

private const val DATABASE_NAME = "vibely_pos.db"
private const val DATABASE_VERSION = 1

/**
 * Room database for the local (offline-first) backend strategy.
 *
 * Contains all entities needed to run the POS system without a Supabase connection.
 * Use [createDatabase] to obtain a JVM instance backed by [BundledSQLiteDriver].
 */
@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    /** Returns the DAO for product operations. */
    abstract fun productDao(): ProductDao

    /** Returns the DAO for category operations. */
    abstract fun categoryDao(): CategoryDao

    /** Returns the DAO for customer operations. */
    abstract fun customerDao(): CustomerDao

    /** Returns the DAO for sale and sale item operations. */
    abstract fun saleDao(): SaleDao

    /** Returns the DAO for payment operations. */
    abstract fun paymentDao(): PaymentDao
}

/**
 * Creates the [AppDatabase] instance for JVM using [BundledSQLiteDriver].
 *
 * The database file is stored at [DATABASE_NAME] relative to the working directory.
 * All DAO queries are dispatched on [Dispatchers.IO].
 *
 * @return Initialized [AppDatabase]
 */
fun createDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(DATABASE_NAME)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
