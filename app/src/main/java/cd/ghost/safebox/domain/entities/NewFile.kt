package cd.ghost.safebox.domain.entities

data class NewFile(
    val fileName: String,
    val name: String,
    val type: FileType,
    val path: String,
    val createdAt: Long,
    val size: String,
    val key: String,
)