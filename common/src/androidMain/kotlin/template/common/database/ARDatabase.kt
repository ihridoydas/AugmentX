package template.common.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ARItemEntity::class], version = 1)
abstract class ARDatabase : RoomDatabase() {
    abstract fun arItemDao(): ARItemDao
}
