package cd.ghost.safebox.data

import android.content.Context
import android.util.Base64
import cd.ghost.safebox.domain.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cd.ghost.safebox.domain.entities.ListType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safe.box.data.store")

class DataStoreOperationsImpl @Inject constructor(
    @ApplicationContext context: Context
) : AppSettings {

    companion object {
        private const val PASSWORD_KEY = "app.password.key"
        private const val LIST_TYPE_KEY = "list.type.key"
        private const val BIOMETRIC_KEY = "biometric.key"
    }

    private object PreferencesKey {
        val passwordKey = stringPreferencesKey(name = PASSWORD_KEY)
        val listTypeKey = stringPreferencesKey(name = LIST_TYPE_KEY)
        val biometricKey = booleanPreferencesKey(name = BIOMETRIC_KEY)
    }

    private val dataStore = context.dataStore

    override fun getPassword(): Flow<String> {
        val al = runBlocking(Dispatchers.IO) {
            dataStore.data.firstOrNull()
        }


        return dataStore.data.map { settings ->
            val password = settings[PreferencesKey.passwordKey] ?: ""
            val decode = Base64.decode(password, Base64.DEFAULT)
            String(decode, StandardCharsets.UTF_8)
        }
    }


    override suspend fun savePassword(password: String) {
        dataStore.edit { settings ->
            val encodedPassword = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)!!
            settings[PreferencesKey.passwordKey] = encodedPassword
        }
    }

    override fun checkPassword(password: String): Flow<Boolean> {
        return dataStore.data.map { settings ->
            settings[PreferencesKey.passwordKey] == password
        }
    }

    override suspend fun updatePassword(newPassword: String) {
        dataStore.edit { settings ->
            val encodedPassword = Base64.encodeToString(newPassword.toByteArray(), Base64.DEFAULT)!!
            settings[PreferencesKey.passwordKey] = encodedPassword
        }
    }

    override fun getListType(): Flow<ListType> {
        return dataStore.data.map { settings ->
            val listType = settings[PreferencesKey.listTypeKey] ?: ListType.LIST_TYPE_COLUMN.label
            if (listType == ListType.LIST_TYPE_COLUMN.label) {
                return@map ListType.LIST_TYPE_COLUMN
            } else {
                return@map ListType.LIST_TYPE_GRID
            }
        }
    }

    override fun getBiometric(): Flow<Boolean> {
        return dataStore.data.map { settings ->
            return@map settings[PreferencesKey.biometricKey] ?: false
        }
    }

    override suspend fun toggleBiometric(value: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKey.biometricKey] = value
        }
    }

    override suspend fun changeListType() {
        dataStore.edit { settings ->
            val listType = settings[PreferencesKey.listTypeKey]
            val newType = if (listType == ListType.LIST_TYPE_COLUMN.label) {
                ListType.LIST_TYPE_GRID.label
            } else {
                ListType.LIST_TYPE_COLUMN.label
            }
            settings[PreferencesKey.listTypeKey] = newType
        }
    }
}