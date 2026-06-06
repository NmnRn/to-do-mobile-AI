package com.odak.app.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.BuildConfig
import com.odak.app.R
import com.odak.app.util.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Available(val sha: String) : UpdateUiState
    data object Downloading : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class UpdateViewModel(app: Application) : AndroidViewModel(app) {

    val currentSha: String = BuildConfig.GIT_SHA
    val versionName: String = BuildConfig.VERSION_NAME

    var state by mutableStateOf<UpdateUiState>(UpdateUiState.Idle)
        private set

    fun check() {
        if (state is UpdateUiState.Checking || state is UpdateUiState.Downloading) return
        state = UpdateUiState.Checking
        viewModelScope.launch {
            val sha = withContext(Dispatchers.IO) { UpdateManager.fetchLatestSha(BuildConfig.REPO) }
            state = when {
                sha == null -> UpdateUiState.Error(getApplication<Application>().getString(R.string.err_no_connection))
                sha == currentSha -> UpdateUiState.UpToDate
                else -> UpdateUiState.Available(sha)
            }
        }
    }

    fun downloadAndInstall() {
        if (state is UpdateUiState.Downloading) return
        state = UpdateUiState.Downloading
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    UpdateManager.downloadApk(getApplication(), BuildConfig.APK_URL)
                }
                UpdateManager.installApk(getApplication(), file)
                state = UpdateUiState.Idle
            } catch (e: Exception) {
                state = UpdateUiState.Error(getApplication<Application>().getString(R.string.err_download_failed))
            }
        }
    }
}
