package com.iamacat.appbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SectionDao {

    @Query("SELECT * FROM sections ORDER BY sort_order ASC")
    abstract fun observeAll(): Flow<List<AppSectionEntity>>

    @Query("SELECT * FROM sections ORDER BY sort_order ASC")
    abstract suspend fun getAll(): List<AppSectionEntity>

    @Query("SELECT * FROM sections WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: String): AppSectionEntity?

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM sections")
    abstract suspend fun maxOrder(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(section: AppSectionEntity)

    @Update
    abstract suspend fun update(section: AppSectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    @Query("DELETE FROM sections WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE sections SET sort_order = :order WHERE id = :id")
    abstract suspend fun updateOrder(id: String, order: Int)

    @Query("UPDATE sections SET is_default = 0")
    abstract suspend fun clearDefaultFlags()

    @Query("UPDATE sections SET is_default = 1 WHERE id = :id")
    abstract suspend fun markDefault(id: String)

    @Transaction
    open suspend fun setDefault(id: String) {
        clearDefaultFlags()
        markDefault(id)
    }

    @Transaction
    open suspend fun replaceOrder(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id -> updateOrder(id, index) }
    }
}
