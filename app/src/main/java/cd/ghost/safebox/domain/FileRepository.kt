package cd.ghost.safebox.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import cd.ghost.safebox.data.FileNotFoundException
import cd.ghost.safebox.domain.entities.FilePrev

interface FileRepository {

    fun getFiles(): Flow<List<FilePrev>>

    /**
     * @throws FileNotFoundException
     * @throws Exception
     */
    suspend fun encodeFile(uri: Uri): Boolean

    /**
     * Decode file and provide path
     * @throws FileNotFoundException
     * @throws Exception
     */
    suspend fun decodeFile(id: Long): String

    suspend fun decodeFiles(ids: List<Long>)

    suspend fun openFile(id: Long): String

    suspend fun deleteFile(file: FilePrev): Boolean

    suspend fun deleteFile(files: List<FilePrev>)

    suspend fun deleteAllFiles(): Boolean

    suspend fun saveFiles(list: List<FilePrev>)

}