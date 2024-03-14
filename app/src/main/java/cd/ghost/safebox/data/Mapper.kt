package cd.ghost.safebox.data

import cd.ghost.safebox.data.database.FileEntity
import cd.ghost.safebox.domain.entities.FilePrev
import cd.ghost.safebox.domain.entities.FileType
import cd.ghost.safebox.domain.entities.NewFile

fun FileEntity.toFilePreview(): FilePrev {
    return FilePrev(
        id = id,
        fileName = fileName,
        name = name,
        fileType = FileType.findType(type),
        path = path,
        createdAt = createdAt,
        size = size,
        key = key
    )
}

fun FilePrev.toFileEntity(): FileEntity {
    return FileEntity(
        id = id,
        fileName = fileName,
        name = name,
        type = fileType.type,
        path = path,
        createdAt = createdAt,
        size = size,
        key = key
    )
}

fun NewFile.toFileEntity(): FileEntity {
    return FileEntity(
        fileName = fileName,
        name = name,
        type = type.type,
        path = path,
        createdAt = createdAt,
        size = size,
        key = key
    )
}