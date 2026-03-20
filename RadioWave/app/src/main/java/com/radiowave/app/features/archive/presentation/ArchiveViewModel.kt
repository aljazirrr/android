package com.radiowave.app.features.archive.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.Recording
import com.radiowave.app.features.archive.domain.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val recordings: List<Recording> = emptyList(),
    val totalSize: Long = 0L,
    val showRenameDialog: Boolean = false,
    val selectedRecording: Recording? = null
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            archiveRepository.getRecordings().collect { recordings ->
                _uiState.value = _uiState.value.copy(recordings = recordings)
            }
        }
        loadTotalSize()
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            archiveRepository.deleteRecording(recording.id)
            loadTotalSize()
        }
    }

    fun renameRecording(id: Long, newTitle: String) {
        viewModelScope.launch {
            archiveRepository.renameRecording(id, newTitle)
            _uiState.value = _uiState.value.copy(showRenameDialog = false)
        }
    }

    fun showRenameDialog(recording: Recording) {
        _uiState.value = _uiState.value.copy(
            showRenameDialog = true,
            selectedRecording = recording
        )
    }

    fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(showRenameDialog = false, selectedRecording = null)
    }

    private fun loadTotalSize() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                totalSize = archiveRepository.getTotalSize()
            )
        }
    }
}
