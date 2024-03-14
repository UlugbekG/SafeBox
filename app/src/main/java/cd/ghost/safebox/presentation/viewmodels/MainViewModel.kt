package cd.ghost.safebox.presentation.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import cd.ghost.safebox.R
import cd.ghost.safebox.data.FileNotFoundException
import cd.ghost.safebox.core.Resources
import cd.ghost.safebox.domain.collectEvents
import cd.ghost.safebox.domain.publish
import cd.ghost.safebox.data.DataNotFoundException
import cd.ghost.safebox.data.FileCreationException
import cd.ghost.safebox.data.FileUnmountedException
import cd.ghost.safebox.domain.AppSettings
import cd.ghost.safebox.domain.DoActivityRequests
import cd.ghost.safebox.domain.entities.FilePrev
import cd.ghost.safebox.domain.FileRepository
import cd.ghost.safebox.domain.LiveEvent
import cd.ghost.safebox.domain.MutableLiveEvent
import cd.ghost.safebox.domain.entities.ItemFile
import cd.ghost.safebox.domain.entities.ListType
import cd.ghost.safebox.presentation.viewmodels.MainViewModel.SelectionMode.Enabled
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel @AssistedInject constructor(
    private val repository: FileRepository,
    @Assisted private val doActivityRequests: DoActivityRequests,
    private val appSettings: AppSettings,
    private val resources: Resources,
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val selectionModeFlow = MutableStateFlow<SelectionMode>(SelectionMode.Disabled)
    private val loading = MutableStateFlow(false)

    val password: LiveData<String> = appSettings.getPassword().asLiveData()

    // check if app is active
    val isAppActive: LiveData<Boolean> = password.map { it.isNotBlank() }

    // check biometric is available or not
    val biometricIsAllowed = appSettings.getBiometric().asLiveData()

    val stateValue = combine(
        selectionModeFlow, repository.getFiles(), loading, appSettings.getListType(), ::merge
    ).asLiveData()

    private val _message = MutableLiveEvent<String>()
    val message: LiveEvent<String> = _message

    init {
        // Get chosen data from external storage
        viewModelScope.launch {
            doActivityRequests.getContentFlow().collectEvents {
                repository.encodeFile(it)
            }
        }
    }

    // toggle biometric is true to false else to true
    fun toggleBiometric(value:Boolean) {
        viewModelScope.launch {
            appSettings.toggleBiometric(value)
        }
    }

    // Save password and navigate to the main screen. This for first launch for the application.
    fun savePassword(password: String) {
        viewModelScope.launch {
            appSettings.savePassword(password)
        }
    }

    private fun merge(
        selectionMode: SelectionMode,
        fileList: List<FilePrev>,
        loading: Boolean,
        listType: ListType,
    ): State {
        val countOfSelectedItems = if (selectionMode is Enabled) {
            selectionMode.selectedIds.size
        } else {
            0
        }
        return State(
            data = fileList.map {
                ItemFile(
                    file = it,
                    isSelected = selectionMode is Enabled && selectionMode.selectedIds.contains(
                        it.id
                    ),
                )
            },
            loading = loading,
            listType = listType,
            showDeleteAction = countOfSelectedItems > 0,
            showDetailsAction = countOfSelectedItems == 1,
            showDecryptAction = countOfSelectedItems > 0
        )
    }

    fun onToggle(item: ItemFile) {
        val selectionMode = selectionModeFlow.value
        if (selectionMode is Enabled) {
            val selectedIds = selectionMode.selectedIds
            if (selectedIds.contains(item.file.id)) {
                selectedIds.remove(item.file.id)
            } else {
                selectedIds.add(item.file.id)
            }
            selectionModeFlow.value = Enabled(selectedIds)
        } else {
            selectionModeFlow.value = Enabled(mutableSetOf(item.file.id))
        }
    }

    fun deleteSelected() {
        enableLoading()
        val selectionMode = selectionModeFlow.value
        if (selectionMode is Enabled) {
            viewModelScope.launch {
                val currentState = stateValue.value ?: return@launch
                val fileItemsToBeDeleted =
                    currentState.data.filter { selectionMode.selectedIds.contains(it.file.id) }
                        .map { it.file }
                repository.deleteFile(fileItemsToBeDeleted)
                if (fileItemsToBeDeleted.size == currentState.data.size) {
                    selectionModeFlow.value = SelectionMode.Disabled
                } else {
                    selectionModeFlow.value = Enabled()
                }
            }
            if (selectionMode.selectedIds.size > 1) {
                _message.publish(resources(R.string.files_have_been_successfully_deleted))
            } else {
                _message.publish(resources(R.string.file_has_been_successfully_deleted))
            }
        }
        disableLoading()
    }

    fun onBackPressed(): Boolean {
        if (selectionModeFlow.value is Enabled) {
            selectionModeFlow.value = SelectionMode.Disabled
            return true
        }
        return false
    }

    fun decodeSelected() {
        enableLoading()
        val selectionMode = selectionModeFlow.value
        if (selectionMode is Enabled) {
            viewModelScope.launch {
                val currentState = stateValue.value ?: return@launch
                val fileItemsToBeDecoded =
                    currentState.data.filter { selectionMode.selectedIds.contains(it.file.id) }
                        .map { it.file.id }
                repository.decodeFiles(fileItemsToBeDecoded)
                if (fileItemsToBeDecoded.size == currentState.data.size) {
                    selectionModeFlow.value = SelectionMode.Disabled
                } else {
                    selectionModeFlow.value = Enabled()
                }
                if (selectionMode.selectedIds.size > 1) {
                    _message.publish(resources(R.string.files_have_been_successfully_decoded))
                } else {
                    _message.publish(resources(R.string.file_has_been_successfully_decoded))
                }
            }
        }
        disableLoading()
    }

    fun requestForContent() {
        doActivityRequests.requestActivityResultForContent()
    }

    fun openSelected() {
        val selectionMode = selectionModeFlow.value
        if (selectionMode is Enabled && selectionMode.selectedIds.size == 1) {
            enableLoading()
            viewModelScope.launch {
                try {
                    val fileId = selectionMode.selectedIds.first()
                    val path = repository.openFile(fileId)
                    doActivityRequests.openFile(path)
                } catch (e: FileNotFoundException) {
                    _message.publish(resources(R.string.file_not_found))
                } catch (e: FileUnmountedException) {
                    _message.publish(resources(R.string.external_storage_unmounted))
                } catch (e: FileCreationException) {
                    _message.publish(resources(R.string.occurred_error_while_creating_file))
                } catch (e: Exception) {
                    _message.publish(resources(R.string.something_went_wrong))
                }
            }
            disableLoading()
        } else {
            _message.publish(resources(R.string.something_went_wrong))
        }
    }

    fun encodeFile(block: () -> Uri) {
        enableLoading()
        viewModelScope.launch {
            try {
                val result = repository.encodeFile(block())
                _message.publish(resources(R.string.file_has_been_successfully_encoded))
                Log.d(TAG, "encodeFile: $result")
            } catch (e: FileNotFoundException) {
                _message.publish(resources(R.string.file_not_found))
            } catch (e: FileUnmountedException) {
                _message.publish(resources(R.string.external_storage_unmounted))
            } catch (e: DataNotFoundException) {
                _message.publish(resources(R.string.data_with_this_id_not_found))
            } catch (e: FileCreationException) {
                _message.publish(resources(R.string.occurred_error_while_creating_file))
            } catch (e: Exception) {
                _message.publish(resources(R.string.something_went_wrong))
            } finally {
                disableLoading()
            }
        }
    }

    private fun enableLoading() {
        loading.value = true
    }

    private fun disableLoading() {
        loading.value = false
    }

    fun changeListType() {
        viewModelScope.launch {
            appSettings.changeListType()
        }
    }

    data class State(
        val data: List<ItemFile> = listOf(),
        val loading: Boolean,
        val listType: ListType,
        val showDeleteAction: Boolean,
        val showDetailsAction: Boolean,
        val showDecryptAction: Boolean,
    ) {
        val showActionsPanel: Boolean
            get() = showDeleteAction || showDecryptAction || showDetailsAction
    }

    sealed class SelectionMode {
        object Disabled : SelectionMode()
        class Enabled(
            val selectedIds: MutableSet<Long> = mutableSetOf()
        ) : SelectionMode()
    }

    @AssistedFactory
    interface Factory {
        fun create(doActivityRequests: DoActivityRequests): MainViewModel
    }

}