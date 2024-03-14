package cd.ghost.safebox.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFileDao(): FileDao
}