package cd.ghost.safebox.domain

import cd.ghost.safebox.domain.entities.ListType
import kotlinx.coroutines.flow.Flow

interface AppSettings {

    fun getPassword(): Flow<String>

    suspend fun savePassword(password: String)

    fun checkPassword(password: String): Flow<Boolean>

    suspend fun updatePassword(newPassword: String)

    fun getListType(): Flow<ListType>

    fun getBiometric(): Flow<Boolean>

    suspend fun toggleBiometric(value: Boolean)

    suspend fun changeListType()
}