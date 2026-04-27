package com.example.projectinventory.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAllItemsList(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE type = :type AND status = 'AVAILABLE' LIMIT :limit")
    suspend fun getAvailableItemsByType(type: String, limit: Int): List<InventoryItemEntity>

    @Query("UPDATE inventory_items SET status = :status, currentJobId = :jobId WHERE id = :itemId")
    suspend fun updateItemStatus(itemId: String, status: String, jobId: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Update
    suspend fun updateItems(items: List<InventoryItemEntity>)

    @Transaction
    suspend fun assignItemsToJob(jobId: String, requirements: List<Pair<String, Int>>) {
        requirements.forEach { (type, count) ->
            val available = getAvailableItemsByType(type, count)
            val updated = available.map { it.copy(status = "BUSY", currentJobId = jobId) }
            updateItems(updated)
        }
    }

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM jobs ORDER BY date ASC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Delete
    suspend fun deleteJob(job: JobEntity)
}

@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["status"]),
        Index(value = ["type"]),
        Index(value = ["currentJobId"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val status: String,
    val serial: String,
    val currentJobId: String?,
    val imageUrl: String?,
    val dailyRate: Double = 0.0,
    val repairNote: String? = null,
    val repairStartDate: String? = null,
    val repairProgress: Int = 0
)

@Entity(
    tableName = "jobs",
    indices = [Index(value = ["date"])]
)
data class JobEntity(
    @PrimaryKey val id: String,
    val name: String,
    val customer: String,
    val location: String,
    val date: String,
    val teamTime: String,
    val preset: String,
    val notes: String,
    val reminderEnabled: Boolean
)

@Database(entities = [InventoryItemEntity::class, JobEntity::class], version = 12)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
