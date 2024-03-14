package cd.ghost.safebox.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import cd.ghost.safebox.data.GlobalApplicationDirsImpl.Companion.DIRECTORY_NAME
import cd.ghost.safebox.domain.CipherController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


class DefCipherController @Inject constructor(
    @ApplicationContext private val context: Context
) : CipherController {

    companion object {
        private const val ALGORITHM = "AES/ECB/PKCS5Padding"
        private const val TAG = "CipherController"
    }

    @SuppressLint("GetInstance")
    override suspend fun encrypt(inputFile: String, outputFile: String, key: String): String =
        withContext(Dispatchers.IO) {
            try {
                // Convert the key from Base64 string to byte array
                val keyBytes = Base64.decode(key, Base64.DEFAULT)

                // Create a SecretKeySpec from the key bytes
                val secretKey = SecretKeySpec(keyBytes, ALGORITHM)

                // Create a Cipher instance and initialize it for encryption
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)

                // Open the input file stream
                BufferedInputStream(FileInputStream(inputFile)).use { inputStream ->
                    // Open the output file stream
                    BufferedOutputStream(FileOutputStream(outputFile)).use { outputStream ->
                        // Wrap the output stream with CipherOutputStream for encryption
                        CipherOutputStream(outputStream, cipher).use { cipherOutputStream ->
                            // Read the input file and write the encrypted data to the output file
                            val buffer = ByteArray(8192) // Adjust the buffer size as needed
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                cipherOutputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }
                Log.d(
                    TAG,
                    "Encryption successful. Encrypted file saved to: $outputFile"
                )
                outputFile
            } catch (e: Exception) {
                Log.e(TAG, "Error encrypting and writing to file: ${e.message}")
                throw e
            }
        }

    @SuppressLint("GetInstance")
    override suspend fun decrypt(
        inputFile: String,
        outputFile: String,
        key: String,
        name: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (!isExternalStorageWritable()) throw FileUnmountedException()
            // Convert the key from Base64 string to byte array
            val keyBytes = Base64.decode(key, Base64.DEFAULT)

            // Create a SecretKeySpec from the key bytes
            val secretKey = SecretKeySpec(keyBytes, ALGORITHM)

            // Create a Cipher instance and initialize it for decryption
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            // Open the input file stream
            val inputStream = BufferedInputStream(FileInputStream(inputFile))

            // Wrap the input stream with CipherInputStream for decryption
            val cipherInputStream = CipherInputStream(inputStream, cipher)

            // Insert the file into the MediaStore (for Android 10 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Ensure the directory is allowed for content URIs
                val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Environment.DIRECTORY_DOWNLOADS + "/$DIRECTORY_NAME"
                } else {
                    Environment.DIRECTORY_DOWNLOADS
                }

                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        name
                    )
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "application/*"
                    )
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        relativePath
                    )
                }

                val contentUri: Uri? = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                contentUri?.let {
                    // Open an OutputStream for the Uri and write the data
                    val outputStream: OutputStream =
                        context.contentResolver.openOutputStream(it)!!

                    // Read the input file and write the decrypted data to the output file
                    val buffer = ByteArray(8192) // Adjust the buffer size as needed
                    var bytesRead: Int
                    while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    // Close the streams
                    cipherInputStream.close()
                    inputStream.close()
                    outputStream.close()

                    Log.d(TAG, "Decryption successful. Decrypted file saved to: $outputStream")
                }
                return@withContext outputFile
            }

            cipherInputStream.use { cipherInputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting and writing to file: $e")
            throw e
        }
    }

    // Function to check if external storage is writable
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}