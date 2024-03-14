package cd.ghost.safebox.domain

import android.net.Uri
import java.io.File

interface DirectoryManager {

    /**
     * Gets file from uri @return [File]
     * @param uri
     *
     * @throws Exception
     */
    suspend fun getFileFromUri(uri: Uri): Result<File>

    /**
     * Get absolut path of the file from uri and delete the file
     * @param [uri]
     * @throws [FileNotFoundexception]
     * @throws [Exception]
     */
    suspend fun deleteFileWithUri(uri: Uri): Boolean

    /**
     * Gets internal directory with provided [name] if file doesn't exist creates new file with provided [name]
     * @param name
     * */
    fun getInternalFileDirectory(name: String): String

    /**
     * Gets external file directory in [Download] if file doesn't exists creates with provided [name]
     */
    fun getEnvironmentDownloadDirectory(name: String): String

    /**
     * Gets cacheDir path
     */
    fun getCacheDir(): String

    /**
     * Deletes with given [path]
     */
    suspend fun deleteFile(path: String): Boolean

}