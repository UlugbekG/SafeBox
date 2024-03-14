package cd.ghost.safebox.data

import cd.ghost.safebox.domain.DirectoryManager
import cd.ghost.safebox.domain.GlobalApplicationDirs
import javax.inject.Inject

class GlobalApplicationDirsImpl @Inject constructor(
    private val directoryManager: DirectoryManager
) : GlobalApplicationDirs {

    companion object {
        const val DIRECTORY_NAME = "Safebox"
    }

    override fun getEncodeDirectory(): String {
        return directoryManager.getInternalFileDirectory(DIRECTORY_NAME)
    }

    override fun getDownloadDirectory(): String {
        return directoryManager.getEnvironmentDownloadDirectory(DIRECTORY_NAME)
    }

    override fun getCacheDirectory(): String {
        return directoryManager.getCacheDir()
    }
}