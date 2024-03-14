package cd.ghost.safebox.domain

interface GlobalApplicationDirs {

    fun getEncodeDirectory(): String

    fun getDownloadDirectory(): String

    fun getCacheDirectory(): String

}