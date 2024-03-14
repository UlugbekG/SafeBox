package cd.ghost.safebox.data

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import cd.ghost.safebox.domain.DoActivityRequests
import cd.ghost.safebox.domain.Event
import cd.ghost.safebox.domain.publish
import cd.ghost.safebox.presentation.MainActivity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.IOException


class DoActivityResultsImpl(
    private val activity: MainActivity
) : DoActivityRequests {

    companion object {
        private const val TAG = "DoActivityResultsImpl"
    }

    private val contentUriLiveData = MutableLiveData<Event<Uri>>()

    private val _reqActivityResultForContent = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data
                ?: throw DataNotFoundException("Data for activity result not found exception!")
            val takeFlags =
                (intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
            activity.contentResolver.takePersistableUriPermission(uri, takeFlags)
            contentUriLiveData.publish(uri)
        }
    }

    private val intent = Intent(Intent.ACTION_GET_CONTENT).also {
        it.addCategory(Intent.CATEGORY_OPENABLE)
        it.type = "*/*"
    }

    private val intentSenderLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == ComponentActivity.RESULT_OK) {
                Toast.makeText(activity, "File deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "File couldn't be deleted!", Toast.LENGTH_SHORT).show()
            }
        }

    fun deleteFileFromUri(uri: Uri) {
        try {
            activity.contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {

            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        activity.contentResolver, listOf(uri)
                    ).intentSender
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }

                else -> {
                    null
                }
            }

            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
    }

    override fun getContentFlow(): Flow<Event<Uri>> = contentUriLiveData.asFlow()

    override fun requestActivityResultForContent() {
        _reqActivityResultForContent.launch(intent)
    }

    override fun openFile(path: String) {
        Log.d(TAG, "openFile: $path")
//        val uri = Uri.fromFile(File(path))
//        Intent().apply {
//            setDataAndType(uri, "*/*")
//            action = Intent.ACTION_VIEW
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            activity.startActivity(intent)
//        }
        openFilee(File(path))
    }

    @Throws(IOException::class)
    fun openFilee(url: File) {
        // Create URI
        val uri = Uri.fromFile(url)
        Log.d("pathAttach", uri.toString())
        val intent = Intent(Intent.ACTION_VIEW)
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav")
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (url.toString().contains(".jpg") || url.toString()
                .contains(".jpeg") || url.toString().contains(".png")
        ) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (url.toString().contains(".3gp") || url.toString()
                .contains(".mpg") || url.toString().contains(".mpeg") || url.toString()
                .contains(".mpe") || url.toString().contains(".mp4") || url.toString()
                .contains(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }
}