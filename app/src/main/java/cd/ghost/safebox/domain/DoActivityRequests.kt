package cd.ghost.safebox.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface DoActivityRequests {

    fun getContentFlow(): Flow<Event<Uri>>

    fun requestActivityResultForContent()

    fun openFile(path: String)

}