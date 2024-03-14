package cd.ghost.safebox.data

open class AppException(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)

class FileNotFoundException(message: String? = null) : AppException(message)
class FileUnmountedException(message: String? = null) : AppException(message)
class DataNotFoundException(message: String? = null) : AppException(message)
class FileCreationException(message: String? = null, cause: Throwable? = null) : AppException(message, cause)