package com.iamacat.appbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MembershipDao {

    @Query("SELECT * FROM memberships WHERE section_id = :sectionId ORDER BY sort_order ASC")
    abstract fun observeForSection(sectionId: String): Flow<List<AppMembershipEntity>>

    @Query("SELECT * FROM memberships WHERE section_id = :sectionId ORDER BY sort_order ASC")
    abstract suspend fun getForSection(sectionId: String): List<AppMembershipEntity>

    @Query("SELECT package_name FROM memberships WHERE section_id = :sectionId")
    abstract suspend fun packagesIn(sectionId: String): List<String>

    @Query("SELECT section_id FROM memberships WHERE package_name = :packageName")
    abstract suspend fun sectionsContaining(packageName: String): List<String>

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM memberships WHERE section_id = :sectionId")
    abstract suspend fun maxOrder(sectionId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(members: List<AppMembershipEntity>)

    @Query("DELETE FROM memberships WHERE section_id = :sectionId AND package_name = :packageName")
    abstract suspend fun delete(sectionId: String, packageName: String)

    @Query("DELETE FROM memberships WHERE section_id = :sectionId")
    abstract suspend fun deleteForSection(sectionId: String)

    @Query("SELECT * FROM memberships WHERE section_id = :sectionId AND package_name = :packageName LIMIT 1")
    abstract suspend fun find(sectionId: String, packageName: String): AppMembershipEntity?

    @Query("UPDATE memberships SET sort_order = :order WHERE section_id = :sectionId AND package_name = :packageName")
    abstract suspend fun setPackageOrder(sectionId: String, packageName: String, order: Int)

    /** Appends the packages that are not yet members; returns how many rows were added. */
    @Transaction
    open suspend fun addMissing(sectionId: String, packageNames: List<String>): Int {
        val existing = packagesIn(sectionId).toHashSet()
        var nextOrder = maxOrder(sectionId) + 1
        val rows = packageNames
            .asSequence()
            .filter { it.isNotBlank() && it !in existing }
            .distinct()
            .map { packageName ->
                existing.add(packageName)
                AppMembershipEntity(sectionId, packageName, nextOrder++)
            }
            .toList()
        insertAll(rows)
        return rows.size
    }

    @Transaction
    open suspend fun moveTo(fromSectionId: String, toSectionId: String, packageName: String): Boolean {
        val source = find(fromSectionId, packageName) ?: return false
        delete(fromSectionId, packageName)
        addMissing(toSectionId, listOf(packageName))
        return source.sectionId != toSectionId
    }

    @Transaction
    open suspend fun replaceOrder(sectionId: String, orderedPackages: List<String>) {
        orderedPackages.forEachIndexed { index, packageName ->
            setPackageOrder(sectionId, packageName, index)
        }
    }
}
