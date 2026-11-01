package com.iamacat.appbox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppSectionEntity::class, AppMembershipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sectionDao(): SectionDao
    abstract fun membershipDao(): MembershipDao
}
