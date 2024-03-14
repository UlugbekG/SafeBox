package cd.ghost.safebox.data

import android.net.Uri
import android.util.Log
import cd.ghost.safebox.core.DateFormatterUtil
import cd.ghost.safebox.data.database.FileDao
import cd.ghost.safebox.domain.CipherController
import cd.ghost.safebox.domain.DirectoryManager
import cd.ghost.safebox.domain.entities.FilePrev
import cd.ghost.safebox.domain.FileRepository
import cd.ghost.safebox.domain.entities.FileType
import cd.ghost.safebox.domain.GlobalApplicationDirs
import cd.ghost.safebox.domain.entities.NewFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val fileDao: FileDao,
    private val cipherController: CipherController,
    private val directoryManager: DirectoryManager,
    private val globalApplicationDirs: GlobalApplicationDirs
) : FileRepository {

    companion object {
        private const val TAG = "FileRepositoryImpl"
    }

    override fun getFiles(): Flow<List<FilePrev>> = fileDao.getFiles().map { listOfEntity ->
        listOfEntity.map { it.toFilePreview() }
    }.flowOn(Dispatchers.IO)

    override suspend fun encodeFile(uri: Uri) = withContext(Dispatchers.IO) {
        val result = directoryManager.getFileFromUri(uri)

        if (result.isSuccess) {
            val file = result.getOrNull() ?: throw FileNotFoundException("File not found!")

            val createdAt = System.currentTimeMillis()
            val fullNameInArray = file.name.split(".")

            // Pick the file name from full file name from separated by split operation
            val fileName = if (fullNameInArray.size > 1) {
                fullNameInArray.dropLast(1).joinToString(".")
            } else {
                fullNameInArray.joinToString()
            }

            // Pick the file type from full file name from separated by split operation
            val type = if (fullNameInArray.size > 1) {
                fullNameInArray.last()
            } else {
                FileType.UNKNOWN.type
            }

            // Check name if the name exist generate with additional expressions with date
            val name = generateName(fileName)

            // Get the file path to decode & save file in the fileDir
            val encodeDir = globalApplicationDirs.getEncodeDirectory()

            val outputFile = StringBuilder()
                .append(encodeDir)
                .append("/")
                .append(name)
                .append(".enc")
                .toString()

            // Generate key
            val key = KeyGeneratorUtil.encodeKeyToBase64(
                key = KeyGeneratorUtil.generateRandomKey()
            )

            // Get the file size
            val fileSizeInBytes = file.readBytes().size
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            val fileSizeInGB = fileSizeInMB / 1024
            val fileSize = when {
                fileSizeInGB > 0 -> "$fileSizeInGB GB"
                fileSizeInMB > 0 -> "$fileSizeInMB MB"
                fileSizeInKB > 0 -> "$fileSizeInKB KB"
                else -> "$fileSizeInBytes Bytes"
            }

            Log.d(TAG, "name: $name")
            Log.d(TAG, "type: $type")
            Log.d(TAG, "size: $fileSize")
            Log.d(TAG, "path: $outputFile")
            Log.d(TAG, "createdAt: $createdAt")

            val newFile = NewFile(
                fileName = "$name.$type",
                name = name,
                type = FileType.findType(type),
                path = outputFile,
                createdAt = createdAt,
                size = fileSize,
                key = key
            )

            // Encode data
            cipherController.encrypt(
                inputFile = file.path,
                outputFile = outputFile,
                key = key
            )
            // Save to database
            fileDao.insert(newFile.toFileEntity())

            // Delete from database
            directoryManager.deleteFile(file.path)
            directoryManager.deleteFileWithUri(uri)
        } else {
            throw Throwable(result.exceptionOrNull())
        }
    }

    override suspend fun openFile(id: Long): String =
        withContext(Dispatchers.IO) {
            val fileEntity = fileDao.getFileById(id)
                ?: throw FileNotFoundException("File with this id not found!")
            val outputFile = globalApplicationDirs.getCacheDirectory()
            val filePath = cipherController.decrypt(
                inputFile = fileEntity.path,
                outputFile = outputFile,
                key = fileEntity.key,
                name = fileEntity.fileName,
            )
            // [filePath] gives file decoded path and we need do attach file name and type.
            return@withContext "$filePath.${fileEntity.name}.${fileEntity.type}"
        }

    override suspend fun decodeFile(id: Long): String =
        withContext(Dispatchers.IO) {
            val fileEntity = fileDao.getFileById(id)
                ?: throw FileNotFoundException("File with this id not found!")
            val outputFile = globalApplicationDirs.getDownloadDirectory()
            cipherController.decrypt(
                inputFile = fileEntity.path,
                outputFile = outputFile,
                key = fileEntity.key,
                name = "${fileEntity.name}.${fileEntity.type}",
            )
        }

    override suspend fun decodeFiles(ids: List<Long>) = withContext(Dispatchers.IO) {
        ids.forEach {
            val fileEntity = fileDao.getFileById(it)
                ?: throw FileNotFoundException("File with this id not found!")
            val outputFile = globalApplicationDirs.getDownloadDirectory()
            cipherController.decrypt(
                inputFile = fileEntity.path,
                outputFile = outputFile,
                key = fileEntity.key,
                name = "${fileEntity.name}.${fileEntity.type}",
            )
        }
    }

    override suspend fun deleteFile(file: FilePrev): Boolean = withContext(Dispatchers.IO) {
        fileDao.delete(file.path)
        directoryManager.deleteFile(file.path)
    }

    override suspend fun deleteFile(files: List<FilePrev>) = withContext(Dispatchers.IO) {
        val entities = files.map { it.toFileEntity() }
        fileDao.delete(entities)
        files.forEach {
            directoryManager.deleteFile(it.path)
        }
    }

    override suspend fun deleteAllFiles(): Boolean = withContext(Dispatchers.IO) {
        fileDao.deleteAll()
        directoryManager.deleteFile(globalApplicationDirs.getEncodeDirectory())
    }

    override suspend fun saveFiles(list: List<FilePrev>) = withContext(Dispatchers.IO) {
        val fileEntities = list.map { it.toFileEntity() }
        fileDao.inset(fileEntities)
    }

    private suspend fun generateName(name: String): String {
        val doesFileExist = fileDao.doesFileExist(name) != null
        val longDateValue = System.currentTimeMillis()
        val df1 = DateFormatterUtil.formatLikeFull(longDateValue)
        return if (doesFileExist) {
            "$name $df1"
        } else {
            name
        }
    }

}