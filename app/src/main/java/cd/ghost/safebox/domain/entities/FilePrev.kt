package cd.ghost.safebox.domain.entities

data class FilePrev(
    val id: Long,
    val name: String,
    val fileName: String,
    val fileType: FileType,
    val path: String,
    val createdAt: Long,
    val size: String,
    val key: String,
)