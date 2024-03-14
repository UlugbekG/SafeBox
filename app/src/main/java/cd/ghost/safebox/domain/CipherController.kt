package cd.ghost.safebox.domain

interface CipherController {

    /**
     * Encrypt file
     * @throws FileCreationException
     * @throws Exception
     */
    suspend fun encrypt(inputFile: String, outputFile: String, key: String): String

    /**
     * Decrypt file
     * @throws FileCreationException
     * @throws Exception
     */
    suspend fun decrypt(inputFile: String, outputFile: String, key: String, name: String): String


}