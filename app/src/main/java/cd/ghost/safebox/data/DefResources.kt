package cd.ghost.safebox.data

import android.content.Context
import cd.ghost.safebox.core.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefResources @Inject constructor(
    @ApplicationContext private val context: Context
) : Resources {
    override fun invoke(id: Int): String {
        return context.getString(id)
    }
}