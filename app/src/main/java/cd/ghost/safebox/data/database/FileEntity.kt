package cd.ghost.safebox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("file")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileName: String,
    val name: String,
    val type: String,
    val path: String,
    val createdAt: Long,
    val size: String,
    val key: String
)