package cd.ghost.safebox.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inset(list: List<FileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: FileEntity)

    @Query("SELECT * FROM file")
    fun getFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM file WHERE id = :id")
    suspend fun getFileById(id: Long): FileEntity?

    @Query("SELECT name FROM file WHERE name = :name")
    suspend fun doesFileExist(name: String): String?

    @Query("DELETE FROM file WHERE path= :path")
    suspend fun delete(path: String)

    @Delete
    suspend fun delete(files: List<FileEntity>)

    @Query("DELETE FROM file")
    suspend fun deleteAll()

    @Transaction
    suspend fun withTransaction(list: List<FileEntity>) {
        deleteAll()
        inset(list)
    }

}